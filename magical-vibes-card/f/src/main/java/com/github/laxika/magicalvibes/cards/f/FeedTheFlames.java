package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "137")
public class FeedTheFlames extends Card {

    public FeedTheFlames() {
        target(TargetFilters.creature())
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addSpellEffect(DealDamageEffect.toTargetCreature(5));
    }
}
