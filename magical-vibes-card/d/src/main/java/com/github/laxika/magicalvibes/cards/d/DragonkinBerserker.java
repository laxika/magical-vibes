package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "131")
@CardRegistration(set = "KHM", collectorNumber = "357")
@CardRegistration(set = "SCD", collectorNumber = "134")
public class DragonkinBerserker extends Card {

    public DragonkinBerserker() {
        addStaticEffect(new ReduceActivatedAbilityCostEffect(new PermanentTruePredicate(),
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER),
                ActivatedAbilityKind.BOAST));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new CreateTokenEffect(
                        "Dragon", 5, 5, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())),
                "Boast — {4}{R}: Create a 5/5 red Dragon creature token with flying. Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
