package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "30")
@CardRegistration(set = "PIP", collectorNumber = "172")
@CardRegistration(set = "PIP", collectorNumber = "458")
@CardRegistration(set = "PIP", collectorNumber = "700")
@CardRegistration(set = "PIP", collectorNumber = "986")
public class SingleCombat extends Card {

    public SingleCombat() {
        addSpellEffect(new EachPlayerSacrificesDownToCountEffect(
                1,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                ))
        ));
        addSpellEffect(new PlayerRestrictionEffect(Subjects.eachPlayer(),
                PlayerRestriction.castingSpellsOf(CardType.CREATURE, CardType.PLANESWALKER),
                Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
