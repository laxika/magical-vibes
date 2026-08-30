package com.github.laxika.magicalvibes.cards.u;

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

@CardRegistration(set = "KHM", collectorNumber = "35")
public class UsherOfTheFallen extends Card {

    public UsherOfTheFallen() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new CreateTokenEffect(
                        1, "Human Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR), Set.of(), Set.of())),
                "Boast — {1}{W}: Create a 1/1 white Human Warrior creature token. Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
