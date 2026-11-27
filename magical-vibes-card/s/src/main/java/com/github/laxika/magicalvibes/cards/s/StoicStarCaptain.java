package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.CrewAndSaddlePowerModifierEffect.Action;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "3")
public class StoicStarCaptain extends Card {

    public StoicStarCaptain() {
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new PowerBoostForCrewAndSaddleEffect(2, Set.of(Action.CREW, Action.STATION)), Subjects.creaturesYouControl()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SeekEffect(new CardSubtypePredicate(CardSubtype.SPACECRAFT))),
                "Exhaust — {1}{W}: Seek a Spacecraft card. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
