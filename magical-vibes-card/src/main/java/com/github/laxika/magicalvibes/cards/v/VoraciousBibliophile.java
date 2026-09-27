package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellTargetCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasAnyTargetPredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "23")
@CardRegistration(set = "TDC", collectorNumber = "63")
public class VoraciousBibliophile extends Card {

    public VoraciousBibliophile() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(TriggeringSpellTargetCount.allTargets())),
                new StackEntryHasAnyTargetPredicate()));
    }
}
