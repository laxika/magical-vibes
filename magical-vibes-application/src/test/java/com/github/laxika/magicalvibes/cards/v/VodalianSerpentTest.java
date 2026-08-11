package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VodalianSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Vodalian Serpent enters without counters when not kicked")
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new VodalianSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent serpent = findSerpent(player1);
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Vodalian Serpent enters with four +1/+1 counters when kicked")
    void entersWithCountersWhenKicked() {
        harness.setHand(player1, List.of(new VodalianSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent serpent = findSerpent(player1);
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Vodalian Serpent can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = new Permanent(new VodalianSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Vodalian Serpent cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        Permanent serpent = new Permanent(new VodalianSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findSerpent(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Vodalian Serpent"))
                .findFirst()
                .orElseThrow();
    }
}
