package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExaltedFlamerOfTzeentch.class, GrizzlyBears.class, Shock.class, LavaAxe.class})
class ExaltedFlamerOfTzeentchTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep returns a random instant or sorcery from the graveyard")
    void upkeepReturnsInstantOrSorcery() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting an instant deals 1 damage to each opponent")
    void castingInstantDamagesEachOpponent() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a creature does not trigger damage")
    void castingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Upkeep returns a sorcery without dealing damage")
    void upkeepReturnsSorceryWithoutCastingIt() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setGraveyard(player1, List.of(new LavaAxe()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lava Axe");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Upkeep returns exactly one of multiple eligible cards")
    void upkeepReturnsExactlyOneEligibleCard() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        Shock instant = new Shock();
        LavaAxe sorcery = new LavaAxe();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(instant, creature, sorcery));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(instant, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(gd.playerHands.get(player1.getId()).getFirst());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Upkeep with no eligible card does not use the opponent's graveyard")
    void upkeepDoesNothingWithoutEligibleCards() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty graveyard causes no choice or return")
    void upkeepWithEmptyGraveyardDoesNothing() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent's upkeep does not return cards")
    void opponentsUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Shock()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Casting a sorcery deals damage before the sorcery resolves")
    void castingSorceryDamagesOnlyOpponent() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Fire of Tzeentch")
    void opponentsInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Flamer triggers independently for the same spell")
    void multipleFlamersEachDealDamage() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }
}
