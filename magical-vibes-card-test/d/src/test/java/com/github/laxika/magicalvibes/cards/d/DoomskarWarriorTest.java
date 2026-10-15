package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BelenonWarAnthem;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfBelenon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomskarWarrior.class, Forest.class, GrizzlyBears.class, Shock.class,
        InvasionOfBelenon.class, BelenonWarAnthem.class})
class DoomskarWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature a combat-damage library ability")
    void backupGrantsCombatDamageAbility() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card nonmatching = new Shock();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching, land, creature));

        castWarriorTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        declareAttackersAndResolveCombat(0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(nonmatching, land, creature);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, creature);
    }

    @Test
    @DisplayName("The source's combat-damage ability uses the damage amount")
    void sourceCombatDamageAbilityUsesDamageAmount() {
        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, land, creature));

        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);
        declareAttackersAndResolveCombat(0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second, third, land, creature);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), creature.getId());
    }

    @Test
    @DisplayName("Backup grants trample as well as the library ability")
    void backupGrantsTrampleThroughABlocker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLife(player2, 20);
        castWarriorTargeting(bears);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class))
                .isNotNull();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(land);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("The optional selection may be declined and only the looked-at cards move")
    void mayDeclineAndBottomOnlyLookedAtCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Shock();
        Card fourth = new Forest();
        Card fifth = new Shock();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, untouched));
        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);

        declareAttackersAndResolveCombat(0);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second, third, fourth, fifth);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(first, second, third, fourth, fifth);
    }

    @Test
    @DisplayName("A short library allows selecting a creature and preserves the remaining cards")
    void selectsCreatureFromShortLibrary() {
        Card creature = new GrizzlyBears();
        Card nonmatching = new Shock();
        harness.setLibrary(player1, List.of(creature, nonmatching));
        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);

        declareAttackersAndResolveCombat(0);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(creature, nonmatching);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    @DisplayName("No eligible cards are automatically put on the bottom without a choice")
    void noMatchingCardsGoToBottom() {
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);

        declareAttackersAndResolveCombat(0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Combat damage with an empty library finishes without a choice or a draw")
    void emptyLibraryDoesNotAttemptToDraw() {
        harness.setLibrary(player1, List.of());
        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);

        declareAttackersAndResolveCombat(0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Backup can target an opposing creature and its controller uses their own library")
    void opposingBackupRecipientUsesItsControllersLibrary() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        Card nonmatching = new Shock();
        Card untouched = new Forest();
        Card ownCard = new Shock();
        harness.setLibrary(player2, List.of(land, creature, nonmatching, untouched));
        harness.setLibrary(player1, List.of(ownCard));
        castWarriorTargeting(bears);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(land, creature, nonmatching);
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    @DisplayName("Combat damage to a battle looks at the damage amount rather than the battle defense")
    void combatDamageToBattleUsesDamageAmount() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfBelenon());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent warrior = addCreatureReady(player1, new DoomskarWarrior());
        Card first = new Shock();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        Card fourth = new Shock();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.setHand(player1, List.of());
        warrior.setAttacking(true);
        warrior.setAttackTarget(battle.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second, third, fourth);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
    }

    @Test
    @DisplayName("Backup's granted ability expires after the turn while the counter remains")
    void backupAbilityExpiresAfterTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castWarriorTargeting(bears);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature));
        harness.setLife(player2, 20);

        declareAttackersAndResolveCombat(0);

        harness.assertLife(player2, 17);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature);
    }

    private void castWarriorTargeting(Permanent target) {
        harness.setHand(player1, List.of(new DoomskarWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castWarriorTargetingItself() {
        harness.setHand(player1, List.of(new DoomskarWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warrior = findPermanent(player1, "Doomskar Warrior");
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();
        return warrior;
    }

    private void declareAttackersAndResolveCombat(int attackerIndex) {
        declareAttackers(List.of(attackerIndex));
        resolveCombat();
        harness.passBothPriorities();
    }
}
