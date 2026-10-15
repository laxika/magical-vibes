package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicTorrent.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class,
        LightningBolt.class, Mountain.class})
class VolcanicTorrentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to opposing creatures and planeswalkers based on spells cast this turn")
    void damagesOpposingCreaturesAndPlaneswalkers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingPlaneswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt(), new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cascade casts a lesser spell and that spell increases the damage")
    void cascadeSpellCountsTowardDamage() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears")
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining cascade returns the exiled cards below the untouched library and deals one damage")
    void decliningCascadeDoesNotIncreaseDamage() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mountain skippedLand = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain untouchedLand = new Mountain();
        harness.setLibrary(player1, List.of(skippedLand, hit, untouchedLand));
        harness.setHand(player1, List.of(new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouchedLand);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only the controller's spells count, including an instant cast after cascade resolves")
    void damageUsesControllerSpellCountAtResolution() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new VolcanicTorrent(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cascade skips lands and equal mana values and returns everything when there is no hit")
    void cascadeWithNoLesserNonlandReturnsAllCards() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mountain land = new Mountain();
        VolcanicTorrent equalManaValue = new VolcanicTorrent();
        harness.setLibrary(player1, List.of(land, equalManaValue));
        harness.setHand(player1, List.of(new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equalManaValue);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Volcanic Torrent");
    }
}
