package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "154")
public class Prizefight extends Card {

    public Prizefight() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
