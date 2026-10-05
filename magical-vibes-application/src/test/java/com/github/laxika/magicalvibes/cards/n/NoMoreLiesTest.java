package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoMoreLies.class, LlanowarElves.class, CarnageTyrant.class, Guile.class})
class NoMoreLiesTest extends BaseCardTest {

    @Test
    @DisplayName("Counters and exiles a spell when its controller cannot pay {3}")
    void countersAndExilesWhenControllerCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Counters and exiles a spell when its controller declines to pay {3}")
    void countersAndExilesWhenControllerDeclinesToPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Leaves a spell to resolve when its controller pays {3}")
    void spellResolvesWhenControllerPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("An uncounterable spell's controller can still choose to pay")
    void uncounterableSpellStillOffersPayment() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tyrant.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Carnage Tyrant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Guile offers a free play when a spell's controller declines payment")
    void decliningPaymentAppliesGuileReplacement() {
        harness.addToBattlefield(player2, new Guile());
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(elves.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can counter and exile its controller's other instant")
    void countersOwnInstant() {
        LlanowarElves elves = new LlanowarElves();
        NoMoreLies firstCounter = new NoMoreLies();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(firstCounter, new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.castAndResolveInstant(player2, 0, firstCounter.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getId().equals(firstCounter.getId()));
        harness.assertInGraveyard(player2, "No More Lies");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Exiling a spell controlled by another player preserves its owner")
    void decliningPaymentPreservesSpellOwner() {
        harness.addToBattlefield(player2, new Guile());
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new NoMoreLies()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new NoMoreLies()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(elves.getId())
                && entry.getControllerId().equals(player2.getId()));
        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(elves.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(c -> c.getId().equals(elves.getId()));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }
}
