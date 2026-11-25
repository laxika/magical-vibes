package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "134")
@CardRegistration(set = "V10", collectorNumber = "7")
@CardRegistration(set = "VMA", collectorNumber = "275")
public class Masticore extends Card {

    public Masticore() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(new DiscardEffect(1, Subjects.you()),
                "Discard a card? If you don't, sacrifice this creature.",
                new SacrificeSelfEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(DealDamageEffect.toTargetCreature(1)),
                "This creature deals 1 damage to target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "Regenerate this creature."
        ));
    }
}
