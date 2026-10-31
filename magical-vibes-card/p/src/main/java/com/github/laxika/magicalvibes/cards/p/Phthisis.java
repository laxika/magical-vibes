package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.TargetPowerPlusToughness;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "122")
@CardRegistration(set = "MMA", collectorNumber = "93")
@CardRegistration(set = "TSR", collectorNumber = "129")
@CardRegistration(set = "C13", collectorNumber = "85")
@CardRegistration(set = "AFC", collectorNumber = "105")
public class Phthisis extends Card {

    public Phthisis() {
        // The target's power and toughness are read from last-known information once it is destroyed.
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new LoseLifeEffect(
                        new Max(new Fixed(0), new TargetPowerPlusToughness()),
                        LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 5—{1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(5));
    }
}
