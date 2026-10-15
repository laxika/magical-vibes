package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesThatCrewedSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "244")
public class LuxuriousLocomotive extends Card {

    public LuxuriousLocomotive() {
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenEffect.ofTreasureToken(new CreaturesThatCrewedSourceThisTurn()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1. Activate only once each turn.",
                1
        ));
    }
}
