package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.r.RhonassStalwart;
import com.github.laxika.magicalvibes.cards.k.KhenraEternal;
import com.github.laxika.magicalvibes.cards.m.MercilessEternal;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnconventionalTactics.class, RhonassStalwart.class, KhenraEternal.class, MercilessEternal.class})
class UnconventionalTacticsTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Resolving gives target creature +3/+3 and flying until end of turn")
    void boostsAndGrantsFlying() {
        prepareMain(player1);
        harness.addToBattlefield(player1, new RhonassStalwart());
        harness.setHand(player1, List.of(new UnconventionalTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {2}{W}

        UUID targetId = harness.getPermanentId(player1, "Rhonas's Stalwart");
        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        prepareMain(player1);
        harness.addToBattlefield(player1, new RhonassStalwart());
        harness.setHand(player1, List.of(new UnconventionalTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Rhonas's Stalwart");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Zombie entering lets you pay {W} to return Unconventional Tactics to hand")
    void zombieEntersPayReturnsToHand() {
        UnconventionalTactics tactics = new UnconventionalTactics();
        harness.setGraveyard(player1, List.of(tactics));
        prepareMain(player1);

        harness.setHand(player1, List.of(new KhenraEternal()));
        harness.addMana(player1, ManaColor.BLACK, 2); // cast the Zombie
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, leaving graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger, opening may-pay prompt

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

        harness.setHand(player1, List.of(new KhenraEternal()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, leaving graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger, opening may-pay prompt

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

        harness.setHand(player1, List.of(new KhenraEternal()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, leaving graveyard trigger on stack
        harness.passBothPriorities(); // resolve trigger, opening may-pay prompt

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

        harness.setHand(player1, List.of(new RhonassStalwart()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Zombie an opponent controls entering does not trigger")
    void opponentZombieDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new UnconventionalTactics()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new KhenraEternal()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOpponentsCreature() {
        prepareMain(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhonassStalwart());
        harness.setHand(player1, List.of(new UnconventionalTactics()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void returningOneCopyDoesNotReturnAnotherCopy() {
        prepareMain(player1);
        harness.setHand(player1, List.of());
        UnconventionalTactics first = new UnconventionalTactics();
        UnconventionalTactics second = new UnconventionalTactics();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.enterBattlefieldAndReturn(player1, new KhenraEternal());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void zombieEnteringDoesNotTriggerTacticsInHand() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new UnconventionalTactics()));

        harness.enterBattlefieldAndReturn(player1, new KhenraEternal());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void oldTriggerCannotReturnCardDiscardedAfterItWasReturnedToHand() {
        prepareMain(player1);
        UnconventionalTactics tactics = new UnconventionalTactics();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(tactics));
        harness.addToBattlefield(player1, new MercilessEternal());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.enterBattlefieldAndReturn(player1, new KhenraEternal());
        harness.enterBattlefieldAndReturn(player1, new KhenraEternal());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Unconventional Tactics");

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Unconventional Tactics");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Unconventional Tactics");
        harness.assertNotInHand(player1, "Unconventional Tactics");
    }
}
