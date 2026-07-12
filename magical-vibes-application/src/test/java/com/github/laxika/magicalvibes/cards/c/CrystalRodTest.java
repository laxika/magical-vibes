package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrystalRodTest extends BaseCardTest {

    // ===== Triggered ability: controller casts blue spell =====

    @Test
    @DisplayName("Controller casts blue spell, accepts may ability, gains 1 life")
    void controllerCastsBlueSpellAndAccepts() {
        harness.addToBattlefield(player1, new CrystalRod());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Crystal Rod"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts blue spell, declines may ability, no life gain")
    void controllerCastsBlueSpellAndDeclines() {
        harness.addToBattlefield(player1, new CrystalRod());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Crystal Rod"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Triggered ability: opponent casts blue spell =====

    @Test
    @DisplayName("Opponent casts blue spell, controller accepts may ability, gains 1 life")
    void opponentCastsBlueSpellControllerAccepts() {
        harness.addToBattlefield(player1, new CrystalRod());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-blue spell does NOT trigger =====

    @Test
    @DisplayName("Non-blue spell does not trigger Crystal Rod")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrystalRod());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
