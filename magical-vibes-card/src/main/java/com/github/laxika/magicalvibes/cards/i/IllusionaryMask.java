package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastCreatureFromHandFaceDownEffect;

import java.util.List;

@CardRegistration(set = "ME3", collectorNumber = "197")
public class IllusionaryMask extends Card {

    public IllusionaryMask() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new CastCreatureFromHandFaceDownEffect()),
                "Cast a creature card from your hand face down without paying its mana cost.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
