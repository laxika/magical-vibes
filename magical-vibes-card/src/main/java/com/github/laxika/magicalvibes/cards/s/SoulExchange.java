package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FEM", collectorNumber = "43")
@CardRegistration(set = "FEM", collectorNumber = "148")
public class SoulExchange extends Card {

    public SoulExchange() {
        addEffect(EffectSlot.SPELL, new ExileCreatureCost());
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .plusOneCountersIfExiledCostCardHasSubtype(CardSubtype.THRULL)
                .counterIfExiledCostCardHasSubtype(CounterType.PLUS_TWO_PLUS_TWO)
                .counterCountIfExiledCostCardHasSubtype(1)
                .build());
    }
}
