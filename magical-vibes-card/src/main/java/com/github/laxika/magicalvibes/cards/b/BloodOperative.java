package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "GRN", collectorNumber = "63")
public class BloodOperative extends Card {

    public BloodOperative() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                "You may exile target card from a graveyard."
        ));
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_SURVEILS,
                new ConditionalEffect(new SourceCardInGraveyard(),
                        new MayPayManaEffect("{0}", 3,
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay 3 life to return Blood Operative from your graveyard to your hand?")));
    }
}
