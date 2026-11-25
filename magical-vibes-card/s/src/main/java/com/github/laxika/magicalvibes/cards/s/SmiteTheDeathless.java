package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOC", collectorNumber = "202")
public class SmiteTheDeathless extends Card {

    public SmiteTheDeathless() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3))
                .addSpellEffect(new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()))
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect());
    }
}
