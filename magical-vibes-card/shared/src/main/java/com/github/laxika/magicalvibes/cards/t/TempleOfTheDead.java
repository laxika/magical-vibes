package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AclazotzDeepestBetrayal;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

/** Back face of {@link AclazotzDeepestBetrayal}. */
public class TempleOfTheDead extends Card {

    public TempleOfTheDead() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new TransformSelfEffect()),
                "{2}{B}, {T}: Transform this land. Activate only if a player has one or fewer cards in hand and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AnyPlayerHandAtMost(1),
                "Activate only if a player has one or fewer cards in hand"
        ));
    }
}
