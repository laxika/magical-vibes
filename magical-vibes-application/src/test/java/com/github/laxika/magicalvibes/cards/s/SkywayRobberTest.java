package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.f.FactOrFiction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywayRobber.class, Bonesplitter.class, GrizzlyBears.class,
        FactOrFiction.class, Harmonize.class, BonecrusherGiant.class, Stomp.class})
class SkywayRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Escape tracks five other cards and offers an exiled artifact for free on combat damage")
    void escapeTracksCardsAndOffersExiledArtifact() {
        Bonesplitter bonesplitter = new Bonesplitter();
        List<Card> exiledForEscape = List.of(
                bonesplitter, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Permanent escapedRobber = escapeWith(exiledForEscape);
        assertThat(gd.getCardsExiledByPermanent(escapedRobber.getId())).containsExactlyInAnyOrderElementsOf(exiledForEscape);

        dealCombatDamage(escapedRobber);

        assertThat(gd.pendingMayAbilities)
                .extracting(PendingMayAbility::targetCardId)
                .containsExactly(bonesplitter.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("A Skyway Robber that did not escape has no combat-damage ability")
    void ordinarySkywayRobberDoesNotGainAbility() {
        Permanent robber = addCreatureReady(player1, new SkywayRobber());
        gd.addToExile(player1.getId(), new Bonesplitter(), robber.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("An escaped Robber can cast a sorcery during combat without paying its mana cost")
    void castsExiledSorceryDuringCombat() {
        Harmonize harmonize = new Harmonize();
        Permanent robber = escapeWith(List.of(harmonize, new SkywayRobber(), new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber()));
        List<Card> drawnCards = List.of(new SkywayRobber(), new SkywayRobber(), new SkywayRobber());
        harness.setLibrary(player1, drawnCards);

        dealCombatDamage(robber);

        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(harmonize.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsAll(drawnCards);
        harness.assertInGraveyard(player1, "Harmonize");
        assertThat(gd.findExiledCard(harmonize.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The instant offer may be declined and the card stays exiled for later combat damage")
    void declinedInstantRemainsAvailable() {
        FactOrFiction instant = new FactOrFiction();
        Permanent robber = escapeWith(List.of(instant, new SkywayRobber(), new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber()));

        dealCombatDamage(robber);

        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(instant.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getCardsExiledByPermanent(robber.getId())).contains(instant);
        assertThat(gd.pendingMayAbilities).isEmpty();

        robber.setTapped(false);
        dealCombatDamage(robber);
        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(instant.getId());
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Only one spell may be cast from the cards exiled for a single combat-damage trigger")
    void acceptingOneOfferWithdrawsOtherOffers() {
        Harmonize sorcery = new Harmonize();
        FactOrFiction instant = new FactOrFiction();
        Permanent robber = escapeWith(List.of(instant, sorcery, new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber()));
        harness.setLibrary(player1, List.of(new SkywayRobber(), new SkywayRobber(), new SkywayRobber()));

        dealCombatDamage(robber);

        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(sorcery.getId(), instant.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(robber.getId())).contains(instant).doesNotContain(sorcery);
        harness.assertInGraveyard(player1, "Harmonize");
    }

    @Test
    @DisplayName("Creature cards and unrelated exiled instants are not offered")
    void ineligibleAndUnrelatedCardsAreNotOffered() {
        Permanent robber = escapeWith(List.of(new SkywayRobber(), new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber(), new SkywayRobber()));
        FactOrFiction unrelated = new FactOrFiction();
        gd.addToExile(player1.getId(), unrelated);

        dealCombatDamage(robber);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.findExiledCard(unrelated.getId())).isNotNull();
    }

    @Test
    @DisplayName("An exiled creature with an instant Adventure can be cast as that Adventure")
    void offersInstantAdventureOfExiledCreature() {
        BonecrusherGiant giant = new BonecrusherGiant();
        Permanent robber = escapeWith(List.of(giant, new SkywayRobber(), new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber()));

        dealCombatDamage(robber);

        assertThat(gd.pendingMayAbilities).extracting(PendingMayAbility::targetCardId)
                .containsExactly(giant.getId());
    }

    @Test
    @DisplayName("Escape cannot be paid with only four other cards in the graveyard")
    void escapeRequiresFiveOtherCards() {
        SkywayRobber robber = new SkywayRobber();
        harness.setGraveyard(player1, List.of(robber, new SkywayRobber(), new SkywayRobber(),
                new SkywayRobber(), new SkywayRobber()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(robber).hasSize(5);
        harness.assertNotOnBattlefield(player1, "Skyway Robber");
    }

    private Permanent escapeWith(List<Card> cards) {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new SkywayRobber());
        graveyard.addAll(cards);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();
        return findPermanent(player1, "Skyway Robber");
    }

    private void dealCombatDamage(Permanent robber) {
        robber.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }
}
