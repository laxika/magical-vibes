package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "158")
@CardRegistration(set = "AFC", collectorNumber = "136")
public class Rile extends Card {

    public Rile() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(DealDamageEffect.toTargetCreature(1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
