package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoShaman.class, Forest.class})
class NantukoShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters and you control no tapped lands")
    void drawsWithNoTappedLands() {
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Draws when you control an untapped land")
    void drawsWithUntappedLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw if a land becomes tapped before its enter trigger resolves")
    void doesNotDrawIfLandBecomesTappedBeforeTriggerResolves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        forest.tap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when you control a tapped land")
    void doesNotTriggerWithTappedLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Opponent's tapped lands do not prevent drawing")
    void drawsWithOpponentsTappedLand() {
        harness.addToBattlefieldAndReturn(player2, new Forest()).tap();
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Tapped nonland permanents do not prevent drawing")
    void drawsWithTappedNonland() {
        harness.addToBattlefieldAndReturn(player1, new NantukoShaman()).tap();
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();

        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Untapping a land after entry does not create a missed trigger")
    void doesNotTriggerRetroactivelyWhenLandUntaps() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        castNantukoShaman();
        harness.passBothPriorities();

        forest.untap();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Suspend exiles Nantuko Shaman with one time counter")
    void suspendExilesWithOneTimeCounter() {
        NantukoShaman card = suspendNantukoShaman();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspended Nantuko Shaman may be cast for free and draws a card")
    void suspendedCardCastsForFreeAndDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        suspendNantukoShaman();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        Permanent permanent = findPermanent(player1, "Nantuko Shaman");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Nantuko Shaman exiled without counters")
    void mayDeclineSuspendCast() {
        NantukoShaman card = suspendNantukoShaman();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Nantuko Shaman");
    }

    private void castNantukoShaman() {
        harness.setHand(player1, List.of(new NantukoShaman()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private NantukoShaman suspendNantukoShaman() {
        NantukoShaman card = new NantukoShaman();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
