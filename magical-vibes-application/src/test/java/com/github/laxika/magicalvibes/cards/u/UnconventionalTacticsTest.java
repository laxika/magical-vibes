package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnconventionalTacticsTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== Spell effect: +3/+3 and flying =====

    @Test
    @DisplayName("Resolving gives target creature +3/+3 and flying until end of turn")
    void boostsAndGrantsFlying() {
        prepareMain(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnconventionalTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {2}{W}

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        prepareMain(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnconventionalTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    // ===== Graveyard Zombie-enters trigger =====

    @Test
    @DisplayName("A Zombie entering lets you pay {W} to return Unconventional Tactics to hand")
    void zombieEntersPayReturnsToHand() {
        UnconventionalTactics tactics = new UnconventionalTactics();
        harness.setGraveyard(player1, List.of(tactics));
        prepareMain(player1);

        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2); // cast the Zombie
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{W}");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(tactics.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(tactics.getId()));
    }

    @Test
    @DisplayName("Declining the Zombie trigger keeps Unconventional Tactics in the graveyard")
    void declineKeepsInGraveyard() {
        UnconventionalTactics tactics = new UnconventionalTactics();
        harness.setGraveyard(player1, List.of(tactics));
        prepareMain(player1);

        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(tactics.getId()));
    }

    @Test
    @DisplayName("Cannot return without paying {W}")
    void cannotReturnWithoutMana() {
        UnconventionalTactics tactics = new UnconventionalTactics();
        harness.setGraveyard(player1, List.of(tactics));
        prepareMain(player1);

        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        // No white mana available to pay the {W}
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(tactics.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(tactics.getId()));
    }

    @Test
    @DisplayName("A non-Zombie creature entering does not trigger")
    void nonZombieDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new UnconventionalTactics()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A Zombie an opponent controls entering does not trigger")
    void opponentZombieDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new UnconventionalTactics()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new WalkingCorpse()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
