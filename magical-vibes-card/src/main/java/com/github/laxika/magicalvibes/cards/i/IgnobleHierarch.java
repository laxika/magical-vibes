package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "906")
public class IgnobleHierarch extends Card {

    public IgnobleHierarch() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. ON_ALLY_CREATURE_ATTACKS records the lone attacker as the non-targeting
        // trigger target, and AttacksAlone restricts the boost to solo attacks.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // {T}: Add {B}, {R}, or {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN))),
                "{T}: Add {B}, {R}, or {G}."
        ));
    }
}
