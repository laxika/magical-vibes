package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                Set<Keyword> grantedKeywords, CardColor animatedColor) implements CardEffect {
}
