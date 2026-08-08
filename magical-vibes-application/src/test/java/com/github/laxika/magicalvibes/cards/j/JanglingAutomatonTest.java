package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JanglingAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps all creatures the defending player controls")
    void attackUntapsDefendingCreatures() {
        Permanent automaton = addCreatureReady(player1, new JanglingAutomaton());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherDefendingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingForest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(defendingForest);

        ownCreature.tap();
        defendingCreature.tap();
        otherDefendingCreature.tap();
        defendingForest.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(defendingCreature.isTapped()).isFalse();
        assertThat(otherDefendingCreature.isTapped()).isFalse();
        assertThat(automaton.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(defendingForest.isTapped()).isTrue();
    }
}
