package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VodalianKnightsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new VodalianKnights()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vodalian Knights");
        harness.assertInGraveyard(player1, "Vodalian Knights");
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWithoutDefendingIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(knightsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWithDefendingIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        gs.declareAttackers(gd, player1, List.of(knightsIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Paying blue mana grants flying until end of turn")
    void payingBlueManaGrantsFlying() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        harness.activateAbility(player1, knightsIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        harness.activateAbility(player1, knightsIndex, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.FLYING)).isFalse();
    }
}
