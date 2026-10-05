package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KomasFaithful.class, FrostBite.class, Swamp.class})
class KomasFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("When Koma's Faithful dies, each player mills three cards")
    void deathTriggerMillsThreeCardsForEachPlayer() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setLibrary(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.addToBattlefield(player1, new KomasFaithful());

        UUID komaId = harness.getPermanentId(player1, "Koma's Faithful");
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, komaId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Koma's Faithful");
        harness.assertInGraveyard(player2, "Frost Bite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One death creates a single trigger for all players")
    void deathCreatesOneTrigger() {
        Permanent faithful = harness.addToBattlefieldAndReturn(player1, new KomasFaithful());
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, faithful.getId());

        harness.assertInGraveyard(player1, "Koma's Faithful");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Milling stops at the end of a short library and still mills the other player")
    void shortLibraryDoesNotPreventOtherPlayerMilling() {
        Swamp onlyCard = new Swamp();
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        Swamp third = new Swamp();
        Swamp remaining = new Swamp();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setLibrary(player2, List.of(first, second, third, remaining));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        Permanent faithful = harness.addToBattlefieldAndReturn(player1, new KomasFaithful());
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, faithful.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second, third).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent milling the other player")
    void emptyLibraryDoesNotPreventOtherPlayerMilling() {
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        Permanent faithful = harness.addToBattlefieldAndReturn(player2, new KomasFaithful());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, faithful.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Koma's Faithful");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lifelink gains life from unblocked combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        Permanent faithful = addCreatureReady(player1, new KomasFaithful());
        faithful.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
