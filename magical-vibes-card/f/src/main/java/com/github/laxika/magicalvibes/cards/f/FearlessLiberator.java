package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "135")
public class FearlessLiberator extends Card {

    public FearlessLiberator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new CreateTokenEffect(
                        "Dwarf Berserker", 2, 1, CardColor.RED,
                        List.of(CardSubtype.DWARF, CardSubtype.BERSERKER), Set.of(), Set.of())),
                "Boast — {2}{R}: Create a 2/1 red Dwarf Berserker creature token. Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
