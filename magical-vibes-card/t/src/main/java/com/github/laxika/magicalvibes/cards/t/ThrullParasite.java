package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "81")
public class ThrullParasite extends Card {

    public ThrullParasite() {
        // Extort — whenever you cast a spell, you may pay {W/B} to drain each opponent for 1.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                        "{W/B}"
                ),
                "Pay {W/B} to extort?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(2), new RemoveCounterFromTargetPermanentEffect()),
                "{T}, Pay 2 life: Remove a counter from target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));
    }
}
