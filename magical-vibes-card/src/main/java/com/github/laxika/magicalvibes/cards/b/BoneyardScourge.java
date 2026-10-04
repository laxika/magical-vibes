package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "C17", collectorNumber = "15")
public class BoneyardScourge extends Card {

    public BoneyardScourge() {
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new MayPayManaEffect("{1}{B}",
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                                "Pay {1}{B} to return Boneyard Scourge from your graveyard to the battlefield?")));
    }
}
