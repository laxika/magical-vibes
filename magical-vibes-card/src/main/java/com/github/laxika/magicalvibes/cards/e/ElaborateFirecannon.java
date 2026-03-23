package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "237")
public class ElaborateFirecannon extends Card {

    public ElaborateFirecannon() {
        // Elaborate Firecannon doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());

        // {4}, {T}: Elaborate Firecannon deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DealDamageToAnyTargetEffect(2)),
                "{4}, {T}: Elaborate Firecannon deals 2 damage to any target."
        ));

        // At the beginning of your upkeep, you may discard a card. If you do, untap Elaborate Firecannon.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new DiscardCardAndUntapSelfEffect(),
                "Discard a card to untap Elaborate Firecannon?"
        ));
    }
}
