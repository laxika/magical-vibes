package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PipBoy3000.class, Forest.class, GrizzlyBears.class})
class PipBoy3000Test extends BaseCardTest {

    private static final String SORT_INVENTORY = "Sort Inventory — Draw a card, then discard a card";
    private static final String PICK_A_PERK = "Pick a Perk — Put a +1/+1 counter on that creature";
    private static final String CHECK_MAP = "Check Map — Untap up to two target lands";

    @Test
    void sortInventoryDrawsThenDiscards() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, SORT_INVENTORY);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void pickAPerkPutsACounterOnTheAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PICK_A_PERK);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checkMapUntapsUpToTwoTargetLands() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        Permanent firstLand = addLandTapped(player1);
        Permanent secondLand = addLandTapped(player1);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, CHECK_MAP);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isFalse();
    }

    @Test
    void unattachedPipBoyDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PipBoy3000());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void equipAttachesToControlledCreatureForTwoMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PipBoy3000());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void sortInventoryCanDiscardTheCardJustDrawnFromAnEmptyHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, SORT_INVENTORY);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void checkMapCanChooseZeroTargetsEvenWhenLandsAreAvailable() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        Permanent land = addLandTapped(player1);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, CHECK_MAP);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void checkMapCanChooseOnlyOneOpponentsLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        Permanent ownLand = addLandTapped(player1);
        Permanent opponentsLand = addLandTapped(player2);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, CHECK_MAP);
        harness.handlePermanentChosen(player1, opponentsLand.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(opponentsLand.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    void pickAPerkStillCountersTheOriginalAttackerAfterEquipmentMoves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PipBoy3000());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PICK_A_PERK);
        equipment.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equipmentControllerChoosesAndLootsWhenOpponentControlsEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PipBoy3000());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(player2, List.of(0));
        harness.handleListChoice(player1, SORT_INVENTORY);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void twoAttachedCopiesChooseBothModesBeforeEitherTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        equip(creature);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, PICK_A_PERK);
        harness.handleListChoice(player1, PICK_A_PERK);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void equip(Permanent creature) {
        Permanent pipBoy = harness.addToBattlefieldAndReturn(player1, new PipBoy3000());
        pipBoy.setAttachedTo(creature.getId());
    }

    private Permanent addLandTapped(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.tap();
        return land;
    }
}
