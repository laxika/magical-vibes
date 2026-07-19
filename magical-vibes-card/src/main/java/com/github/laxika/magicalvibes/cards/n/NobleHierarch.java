package com.github.laxika.magicalvibes.cards.n;

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

@CardRegistration(set = "CON", collectorNumber = "87")
public class NobleHierarch extends Card {

    public NobleHierarch() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. ON_ALLY_CREATURE_ATTACKS fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target, so BoostTargetCreatureEffect boosts "that creature";
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // {T}: Add {G}, {W}, or {U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {G}, {W}, or {U}."
        ));
    }
}
