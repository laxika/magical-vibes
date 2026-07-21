package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BantSojournersTest extends BaseCardTest {

    private long soldierTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    // ===== Death trigger: you may create a 1/1 white Soldier token =====

    @Test
    @DisplayName("When it dies, may create a 1/1 white Soldier token")
    void diesCreatesSoldierToken() {
        harness.addToBattlefield(player1, new BantSojourners());

        killWithFlameJavelin();
        harness.passBothPriorities(); // resolve the death trigger -> may-token choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldierTokenCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger may create nothing — declining leaves no token")
    void diesMayDeclineToken() {
        harness.addToBattlefield(player1, new BantSojourners());

        killWithFlameJavelin();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(soldierTokenCount(player1)).isZero();
    }

    // ===== Cycling reflexive trigger: may create a Soldier token, then draw =====

    @Test
    @DisplayName("Cycling creates a Soldier token and draws a card")
    void cyclingCreatesTokenAndDraws() {
        harness.setHand(player1, List.of(new BantSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldierTokenCount(player1)).isEqualTo(1);
        // The cycling draw still happens: Bant Sojourners discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bant Sojourners"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling may create no token — declining still draws a card")
    void cyclingMayDeclineTokenStillDraws() {
        harness.setHand(player1, List.of(new BantSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(soldierTokenCount(player1)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID bantId = harness.getPermanentId(player1, "Bant Sojourners");
        harness.castInstant(player2, 0, bantId);
        harness.passBothPriorities(); // Flame Javelin resolves -> Bant dies -> death trigger onto stack
    }
}
