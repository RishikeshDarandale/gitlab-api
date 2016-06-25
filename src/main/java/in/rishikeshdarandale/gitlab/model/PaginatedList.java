/*
 *  The MIT License (MIT)
 *  Copyright (c) 2016 Rishikesh Darandale
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions
 *  of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *  TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 *  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 */
package in.rishikeshdarandale.gitlab.model;

import java.util.List;

/**
 * Created by rishikesh on 20/6/16.
 */
public class PaginatedList<T> {
    private List<T> tList;
    private Integer totalItems;
    private Integer totalPages;
    private Integer totalItemsPerPage;
    private Integer currentPage;
    private Integer previousPage;
    private Integer nextPage;

    public PaginatedList(List<T> t, Integer totalItems,
                         Integer totalPages, Integer totalItemsPerPage,
                         Integer currentPage, Integer previousPage, Integer nextPage) {
        this.tList = t;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.totalItemsPerPage = totalItemsPerPage;
        this.currentPage = currentPage;
        this.previousPage = previousPage;
        this.nextPage = nextPage;
    }

    public List<T> gettList() {
        return tList;
    }

    public void settList(List<T> tList) {
        this.tList = tList;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalItemsPerPage() {
        return totalItemsPerPage;
    }

    public void setTotalItemsPerPage(Integer totalItemsPerPage) {
        this.totalItemsPerPage = totalItemsPerPage;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(Integer previousPage) {
        this.previousPage = previousPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }
}
