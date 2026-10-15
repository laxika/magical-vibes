package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EVE", collectorNumber = "116")
public class CankerousThirst extends Card {

    public CankerousThirst() {
        // Each clause has its own "target creature"; both are chosen whatever mana is spent, and
        // they may be the same creature.
        setAllowSharedTargets(true);
        SpellTarget shrinkTarget = target(TargetFilters.creature());
        SpellTarget pumpTarget = target(TargetFilters.creature());

        // If {B} was spent to cast this spell, you may have target creature get -3/-3 until end of turn.
        shrinkTarget.addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK),
                new MayEffect(new BoostTargetCreatureEffect(-3, -3),
                        "Have target creature get -3/-3 until end of turn?")));

        // If {G} was spent to cast this spell, you may have target creature get +3/+3 until end of turn.
        pumpTarget.addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN),
                new MayEffect(new BoostTargetCreatureEffect(3, 3),
                        "Have target creature get +3/+3 until end of turn?")));
    }
}
