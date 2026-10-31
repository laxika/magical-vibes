package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "198")
@CardRegistration(set = "FRF", collectorNumber = "100")
public class FlamewakePhoenix extends Card {

    public FlamewakePhoenix() {
        var ferocious = new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastPredicate(4))));
        var returnToBattlefield = new ReturnSourceCardFromGraveyardToBattlefieldEffect(false);
        var mayPay = new MayPayManaEffect("{R}",
                returnToBattlefield,
                "Pay {R} to return Flamewake Phoenix from your graveyard to the battlefield?");
        addStaticEffect(new MustAttackEffect());
        addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(ferocious, mayPay));
    }
}
