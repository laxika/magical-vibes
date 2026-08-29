package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "FRF", collectorNumber = "100")
public class FlamewakePhoenix extends Card {

    public FlamewakePhoenix() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                        new MayPayManaEffect(
                                "{R}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Pay {R} to return Flamewake Phoenix to the battlefield?"
                        )
                ));
    }
}
