package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.TargetPowerPlusToughness;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "122")
public class Phthisis extends Card {

    public Phthisis() {
        // Life loss resolves first so the target's power and toughness are read before destruction.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                        new Max(new Fixed(0), new TargetPowerPlusToughness()),
                        LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 5—{1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(5));
    }
}
