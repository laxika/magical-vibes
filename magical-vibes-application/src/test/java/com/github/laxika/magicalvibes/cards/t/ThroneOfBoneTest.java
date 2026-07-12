package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThroneOfBoneTest extends BaseCardTest {

    // ===== Controller casts black spell =====

    @Test
    @DisplayName("Controller casts black spell, pays {1}, gains 1 life")
    void controllerCastsBlackSpellAndPays() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Trigger goes on the stack unconditionally
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Throne of Bone"));

        // Resolving the trigger prompts the may-pay choice
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        // Accept and pay {1}
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts black spell, declines to pay, no life gain")
    void controllerCastsBlackSpellAndDeclines() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        // No spare mana to pay {1}

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Opponent casts black spell =====

    @Test
    @DisplayName("Opponent casts black spell, controller pays {1}, gains 1 life")
    void opponentCastsBlackSpellControllerPays() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DuskImp()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Resolve the trigger (controller of Throne of Bone chooses)
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-black spell does NOT trigger =====

    @Test
    @DisplayName("Non-black spell does not trigger Throne of Bone")
    void nonBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Throne of Bone"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
