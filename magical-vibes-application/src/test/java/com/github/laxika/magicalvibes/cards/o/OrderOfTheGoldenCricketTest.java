package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderOfTheGoldenCricketTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking and paying {W} grants flying until end of turn")
    void payingGrantsFlying() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may-pay does not grant flying")
    void decliningDoesNotGrantFlying() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Accepting without mana does not grant flying")
    void cannotPayDoesNotGrantFlying() {
        Permanent cricket = addReadyCricket(player1);
        // No mana added

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyCricket(Player player) {
        Permanent perm = new Permanent(new OrderOfTheGoldenCricket());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
