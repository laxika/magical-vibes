package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "206")
public class Moderation extends Card {

    public Moderation() {
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1, SpellLimitScope.CONTROLLER));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect())));
    }
}
