package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Venomous Words, the prepare spell of Scathing Shadelock // Venomous Words (SOS 98). */
public class VenomousWords extends Card {

    public VenomousWords() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.target()));
    }
}
