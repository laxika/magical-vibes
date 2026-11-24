package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "POR", collectorNumber = "71")
public class Taunt extends Card {

    public Taunt() {
        // During target player's next turn, creatures that player controls attack you if able.
        addSpellEffect(MatchingCreaturesMustAttackThisTurnEffect.controlledBy(Subjects.targetPlayer())
                .attacking(Subjects.you())
                .lasting(Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
    }
}
