package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "311")
public class BramblefortFink extends Card {

    public BramblefortFink() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(new SetBasePowerToughnessEffect(10, 10, GrantScope.SELF)),
                "{8}: This creature has base power and toughness 10/10 until end of turn. Activate only if you control an Oko planeswalker."
        ).withActivationCondition(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.OKO)
                ))),
                "Activate only if you control an Oko planeswalker"));
    }
}
