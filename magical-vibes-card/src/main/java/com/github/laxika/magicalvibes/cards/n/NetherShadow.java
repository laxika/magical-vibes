package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "183")
@CardRegistration(set = "4ED", collectorNumber = "149")
public class NetherShadow extends Card {

    public NetherShadow() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new CardsAboveSelfInGraveyard(3, new CardTypePredicate(CardType.CREATURE)),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Put Nether Shadow onto the battlefield from your graveyard?")));
    }
}
