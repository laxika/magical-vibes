package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "125")
public class CyclicalEvolution extends Card {

    public CyclicalEvolution() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3))
                .addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(),
                "Suspend 3\u2014{2}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
