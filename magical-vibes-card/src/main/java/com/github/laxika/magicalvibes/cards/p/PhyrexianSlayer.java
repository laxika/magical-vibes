package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "118")
public class PhyrexianSlayer extends Card {

    public PhyrexianSlayer() {
        // Whenever Phyrexian Slayer becomes blocked by a white creature, destroy that creature.
        // It can't be regenerated. The trigger is created once per qualifying blocker.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        SequenceEffect.of(
                                new PreventTargetCreatureRegenerationThisTurnEffect(),
                                new DestroyCreatureBlockingThisEffect())),
                TriggerMode.PER_BLOCKER);
    }
}
