package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TargetFilter;

public record WithoutKeywordTargetFilter(Keyword keyword) implements TargetFilter {
}
