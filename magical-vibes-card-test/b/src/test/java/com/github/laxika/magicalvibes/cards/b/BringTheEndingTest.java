package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringTheEnding.class, CopperLonglegs.class})
class BringTheEndingTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller has fewer than three poison counters and cannot pay {2}")
    void countersWhenControllerCannotPay() {
        CopperLonglegs creature = new CopperLonglegs();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BringTheEnding()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.assertNotOnBattlefield(player1, "Copper Longlegs");
    }

    @Test
    void countersWhenControllerDeclinesPaymentDespiteHavingMana() {
        CopperLonglegs creature = new CopperLonglegs();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new BringTheEnding()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.assertNotOnBattlefield(player1, "Copper Longlegs");
    }

    @Test
    void checksCorruptedWhenResolvingRatherThanWhenCast() {
        CopperLonglegs creature = new CopperLonglegs();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        harness.setHand(player2, List.of(new BringTheEnding()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        gd.playerPoisonCounters.put(player1.getId(), 4);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.assertNotOnBattlefield(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Does not counter when its target's controller has two poison counters and pays {2}")
    void allowsSpellWhenBelowCorruptedThresholdAndPays() {
        CopperLonglegs creature = new CopperLonglegs();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        gd.playerPoisonCounters.put(player1.getId(), 2);

        harness.setHand(player2, List.of(new BringTheEnding()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Counters a spell immediately when its controller has three poison counters")
    void countersWhenControllerHasThreePoisonCounters() {
        CopperLonglegs creature = new CopperLonglegs();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        gd.playerPoisonCounters.put(player1.getId(), 3);

        harness.setHand(player2, List.of(new BringTheEnding()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.assertNotOnBattlefield(player1, "Copper Longlegs");
    }
}
