package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "71")
public class GhastlyRemains extends Card {

    public GhastlyRemains() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmplifyEffect(
                1, new CardSubtypePredicate(CardSubtype.ZOMBIE)));
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MayPayManaEffect(
                        "{B}{B}{B}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Pay {B}{B}{B} to return Ghastly Remains to your hand?"));
    }
}
