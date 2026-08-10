package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlimmervoidTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent glimmervoid = harness.addToBattlefieldAndReturn(player1, new Glimmervoid());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 1);
        assertThat(glimmervoid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of any end step when its controller controls no artifacts")
    void sacrificesAtOpponentsEndStepWithNoArtifacts() {
        harness.addToBattlefield(player1, new Glimmervoid());

        advanceToEndStep(player2);

        harness.assertNotOnBattlefield(player1, "Glimmervoid");
        harness.assertInGraveyard(player1, "Glimmervoid");
    }

    @Test
    @DisplayName("Survives the end step while its controller controls an artifact")
    void survivesWithArtifact() {
        harness.addToBattlefield(player1, new Glimmervoid());
        harness.addToBattlefield(player1, new Spellbook());

        advanceToEndStep(player2);

        harness.assertOnBattlefield(player1, "Glimmervoid");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new Glimmervoid());
        harness.addToBattlefield(player2, new Spellbook());

        advanceToEndStep(player2);

        harness.assertNotOnBattlefield(player1, "Glimmervoid");
        harness.assertInGraveyard(player1, "Glimmervoid");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
