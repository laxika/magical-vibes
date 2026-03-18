package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NightbirdsClutches;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PastInFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flashback to instant cards in graveyard")
    void grantsFlashbackToInstantsInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Granted flashback lets you cast an instant from graveyard")
    void grantedFlashbackAllowsCastingInstantFromGraveyard() {
        Shock shock = new Shock();
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        // Past in Flames costs {3}{R}, Shock costs {R}
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Cast and resolve Past in Flames
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Now cast Shock from graveyard with granted flashback
        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Shock should have dealt 2 damage to the creature
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Granted flashback cost equals the card's mana cost")
    void grantedFlashbackCostEqualsManaCost() {
        Shock shock = new Shock();  // Mana cost {R}
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        // Past in Flames costs {3}{R}, Shock flashback costs {R} (its mana cost)
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Cast and resolve Past in Flames
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Cast Shock with granted flashback — costs {R}
        harness.castFlashback(player1, 0, creature.getId());

        // Should have exactly 0 mana left ({3}{R} for Past in Flames + {R} for Shock = {3}{R}{R} total)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Card cast with granted flashback is exiled after resolution")
    void grantedFlashbackExilesAfterResolution() {
        Shock shock = new Shock();
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Does not grant flashback to cards already having flashback")
    void doesNotGrantFlashbackToCardsAlreadyHavingFlashback() {
        NightbirdsClutches clutches = new NightbirdsClutches();
        harness.setGraveyard(player1, List.of(clutches));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Nightbird's Clutches already has flashback, so it should not be in the granted set
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(clutches.getId());
    }

    @Test
    @DisplayName("Granted flashback is cleared at end of turn")
    void grantedFlashbackClearedAtEndOfTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isNotEmpty();

        // Simulate end-of-turn cleanup
        gd.cardsGrantedFlashbackUntilEndOfTurn.clear();

        // Cannot cast the Shock with flashback anymore
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent creature = addReadyCreature(player2);

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
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Past in Flames"));

        // Shock should have gained flashback
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Cannot cast non-flashback card from graveyard without Past in Flames")
    void cannotCastWithoutPastInFlames() {
        Shock shock = new Shock();
        Permanent creature = addReadyCreature(player2);
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Past in Flames goes to graveyard after normal cast")
    void goesToGraveyardAfterNormalCast() {
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Past in Flames"));
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
