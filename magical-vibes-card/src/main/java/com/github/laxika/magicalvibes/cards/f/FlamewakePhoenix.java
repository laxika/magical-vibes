package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "FDN", collectorNumber = "198")
public class FlamewakePhoenix extends Card {

    public FlamewakePhoenix() {
        var ferocious = new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4));
        var returnToBattlefield = new ReturnSourceCardFromGraveyardToBattlefieldEffect(false);
        var mayPay = new MayPayManaEffect("{R}",
                returnToBattlefield,
                "Pay {R} to return Flamewake Phoenix from your graveyard to the battlefield?");
        addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(ferocious, mayPay));
    }
}
