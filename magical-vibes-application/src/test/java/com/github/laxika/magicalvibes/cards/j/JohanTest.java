package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JohanTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the beginning-of-combat ability gives your creatures vigilance and locks Johan from attacking")
    void acceptingAbilityGrantsVigilanceAndPreventsJohanAttacking() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, johan, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the ability leaves Johan able to attack and does not grant vigilance")
    void decliningAbilityDoesNothing() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, johan, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        declareAttackers(player1, List.of(0));
        assertThat(johan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting while Johan is tapped still prevents his attack but grants no vigilance")
    void tappedJohanDoesNotGrantVigilance() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        johan.tap();

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        johan.untap();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
