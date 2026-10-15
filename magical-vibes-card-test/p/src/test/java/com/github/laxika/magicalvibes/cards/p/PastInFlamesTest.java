package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NightbirdsClutches;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PastInFlames.class, Shock.class, GrizzlyBears.class, NightbirdsClutches.class})
class PastInFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flashback to instant cards in graveyard")
    void grantsFlashbackToInstantsInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Granted flashback lets you cast an instant from graveyard")
    void grantedFlashbackAllowsCastingInstantFromGraveyard() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        // Past in Flames costs {3}{R}, Shock costs {R}
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Cast and resolve Past in Flames
        harness.castAndResolveSorcery(player1, 0, 0);

        // Now cast Shock from graveyard with granted flashback
        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Granted flashback cost equals the card's mana cost")
    void grantedFlashbackCostEqualsManaCost() {
        Shock shock = new Shock();  // Mana cost {R}
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        // Past in Flames costs {3}{R}, Shock flashback costs {R} (its mana cost)
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Cast and resolve Past in Flames
        harness.castAndResolveSorcery(player1, 0, 0);

        // Cast Shock with granted flashback — costs {R}
        harness.castFlashback(player1, 0, creature.getId());

        // Should have exactly 0 mana left ({3}{R} for Past in Flames + {R} for Shock = {3}{R}{R} total)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Card cast with granted flashback is exiled after resolution")
    void grantedFlashbackExilesAfterResolution() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Does not grant flashback to creature cards in graveyard")
    void doesNotGrantFlashbackToCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Grants a second flashback cost to cards already having flashback")
    void grantsFlashbackToCardsAlreadyHavingFlashback() {
        NightbirdsClutches clutches = new NightbirdsClutches();
        harness.setGraveyard(player1, List.of(clutches));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Nightbird's Clutches");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clutches);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Granted flashback is cleared at end of turn")
    void grantedFlashbackClearedAtEndOfTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isNotEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        // Cannot cast the Shock with flashback anymore
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Past in Flames itself has flashback {4}{R}")
    void pastInFlamesHasFlashback() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new PastInFlames(), shock));
        // Flashback cost is {4}{R}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Past in Flames should be exiled (flashback exile)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Past in Flames"));

        // Shock should have gained flashback
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Cannot cast non-flashback card from graveyard without Past in Flames")
    void cannotCastWithoutPastInFlames() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can use the granted flashback cost even when the printed cost is affordable")
    void canUseGrantedCostWhenPrintedCostIsAffordable() {
        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightbird's Clutches"));
    }

    @Test
    @DisplayName("Offers flashback when only the granted cost is affordable")
    void offersFlashbackWhenOnlyGrantedCostIsAffordable() {
        harness.setGraveyard(player1, List.of(new NightbirdsClutches()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Does not grant flashback to the opponent's graveyard")
    void doesNotGrantFlashbackToOpponentsCards() {
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cards put into the graveyard after resolution do not gain flashback")
    void doesNotGrantFlashbackToLaterCards() {
        harness.setHand(player1, List.of(new PastInFlames(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertInGraveyard(player1, "Shock");

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player1, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Past in Flames goes to graveyard after normal cast")
    void goesToGraveyardAfterNormalCast() {
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Past in Flames");
    }
}
