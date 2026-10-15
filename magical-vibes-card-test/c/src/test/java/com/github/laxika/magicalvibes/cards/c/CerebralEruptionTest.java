package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CerebralEruption.class, Forest.class, GrizzlyBears.class, Shock.class, Memnite.class})
class CerebralEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cerebral Eruption puts it on the stack")
    void castingPutsItOnStack() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cerebral Eruption");
    }

    @Test
    @DisplayName("Deals damage equal to revealed card's mana value to target player")
    void dealsDamageToPlayer() {
        // Shock has mana value 1
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage equal to revealed card's mana value to target's creatures")
    void dealsDamageToTargetCreatures() {
        // GrizzlyBears on top has mana value 2 — deals 2 damage to each of player2's creatures
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears (2/2) takes 2 damage = lethal, should die
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Player also takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not damage controller's creatures")
    void doesNotDamageControllerCreatures() {
        harness.setLibrary(player2, List.of(new GrizzlyBears())); // mana value 2
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Controller's creature should be unharmed
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("High mana value card deals more damage")
    void highManaValueDealsMoreDamage() {
        // CerebralEruption itself has mana value 4
        harness.setLibrary(player2, List.of(new CerebralEruption()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Returns to hand when a land card is revealed")
    void returnsToHandWhenLandRevealed() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should be in hand, not graveyard
        harness.assertInHand(player1, "Cerebral Eruption");
        harness.assertNotInGraveyard(player1, "Cerebral Eruption");
        // Land has mana value 0 — no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard when a non-land card is revealed")
    void goesToGraveyardWhenNonLandRevealed() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should be in graveyard, not hand
        harness.assertInGraveyard(player1, "Cerebral Eruption");
        harness.assertNotInHand(player1, "Cerebral Eruption");
    }

    @Test
    @DisplayName("Does nothing when target's library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Goes to graveyard (no land revealed)
        harness.assertInGraveyard(player1, "Cerebral Eruption");
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void revealedCardStaysOnTopAndAllOpposingCreaturesTakeDamage() {
        CerebralEruption revealed = new CerebralEruption();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(revealed, second));
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(revealed, second);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof Memnite).hasSize(2);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void zeroManaValueNonlandDealsNoDamageAndDoesNotReturnSpell() {
        Memnite revealed = new Memnite();
        harness.setLibrary(player2, List.of(revealed));
        harness.addToBattlefield(player2, new Memnite());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player1, "Cerebral Eruption");
        harness.assertNotInHand(player1, "Cerebral Eruption");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(revealed);
    }
}
