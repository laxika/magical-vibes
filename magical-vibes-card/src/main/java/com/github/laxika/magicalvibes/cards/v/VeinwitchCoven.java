package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOC", collectorNumber = "228")
public class VeinwitchCoven extends Card {

    public VeinwitchCoven() {
        // Whenever you gain life, you may pay {B}. If you do, return target creature card from your
        // graveyard to your hand.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new MayPayManaEffect("{B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build(),
                "Pay {B} to return target creature card from your graveyard to your hand?"));
    }
}
