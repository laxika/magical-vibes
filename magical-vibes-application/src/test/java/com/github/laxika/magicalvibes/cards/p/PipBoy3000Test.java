package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
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
