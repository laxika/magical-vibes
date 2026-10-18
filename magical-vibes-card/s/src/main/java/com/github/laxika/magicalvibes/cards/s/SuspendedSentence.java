package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "25")
@CardRegistration(set = "DSC", collectorNumber = "54")
public class SuspendedSentence extends Card {

    public SuspendedSentence() {
        // Destroy first, then the target's controller loses 3 life (CR 608.2c). The destroyed
        // creature's controller is still recorded on the stack entry, so the life loss reaches it.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL,
                        new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));

        // Suspend follows the card's own timing (CR 702.62a), so an instant can be suspended at instant speed.
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 3\u2014{1}{B}"
        ).withSuspendsSourceFromHand(3));
    }
}
