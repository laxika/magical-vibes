package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "157")
public class TuskeriFirewalker extends Card {

    public TuskeriFirewalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                "Boast — {1}: Exile the top card of your library. You may play that card this turn. "
                        + "Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
