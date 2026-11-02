package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "206")
public class Moderation extends Card {

    public Moderation() {
        addStaticEffect(new LimitSpellsPerTurnEffect(1, SpellLimitScope.CONTROLLER));
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect())));
    }
}
