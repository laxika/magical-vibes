package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "70")
public class PymParticles extends Card {

    public PymParticles() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.target()))
                .addSpellEffect(new MakeCreatureUnblockableEffect())
                .addSpellEffect(new DrawCardEffect());
    }
}
