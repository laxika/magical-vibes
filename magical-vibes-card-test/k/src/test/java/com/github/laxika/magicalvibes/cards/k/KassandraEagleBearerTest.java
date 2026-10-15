package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BrotherhoodRegalia;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheSpearOfLeonidas;
import com.github.laxika.magicalvibes.cards.u.UmezawasJitte;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KassandraEagleBearer.class, GrizzlyBears.class, TheSpearOfLeonidas.class,
        UmezawasJitte.class, BrotherhoodRegalia.class, PsychogenicProbe.class})
class KassandraEagleBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with The Spear of Leonidas from the graveyard")
    void searchesGraveyardForTheSpearOfLeonidas() {
        harness.setGraveyard(player1, List.of(new TheSpearOfLeonidas()));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertNotInGraveyard(player1, "The Spear of Leonidas");
    }

    @Test
    @DisplayName("Searches the library for The Spear of Leonidas")
    void searchesLibraryForTheSpearOfLeonidas() {
        harness.setLibrary(player1, List.of(new TheSpearOfLeonidas()));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
    }

    @Test
    @DisplayName("Draws when a creature with a legendary Equipment attached deals combat damage")
    void drawsForCombatDamageWithLegendaryEquipment() {
        addCreatureReady(player1, new KassandraEagleBearer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent jitte = harness.addToBattlefieldAndReturn(player1, new UmezawasJitte());
        jitte.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw for a nonlegendary Equipment")
    void doesNotDrawForNonlegendaryEquipment() {
        addCreatureReady(player1, new KassandraEagleBearer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BrotherhoodRegalia());
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void castKassandra() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveKassandraEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void searchesHandForTheSpearOfLeonidas() {
        harness.setHand(player1, List.of(new KassandraEagleBearer(), new TheSpearOfLeonidas()));

        castKassandra();
        resolveKassandraEnterTrigger();

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertNotInHand(player1, "The Spear of Leonidas");
    }

    @Test
    void shufflesAfterFindingTheSpearInGraveyard() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new TheSpearOfLeonidas()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertLife(player1, 18);
    }

    @Test
    void shufflesAfterFindingTheSpearInHand() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new KassandraEagleBearer(), new TheSpearOfLeonidas()));

        castKassandra();
        resolveKassandraEnterTrigger();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertLife(player1, 18);
    }

    @Test
    void drawsForKassandraHerselfWithTheSpearAttached() {
        Permanent attacker = addCreatureReady(player1, new KassandraEagleBearer());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfLeonidas());
        spear.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void mayFailToFindTheSpearWhenItIsOnlyInHand() {
        harness.setHand(player1, List.of(new KassandraEagleBearer(), new TheSpearOfLeonidas()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castKassandra();
        resolveKassandraEnterTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "The Spear of Leonidas");
        harness.assertNotOnBattlefield(player1, "The Spear of Leonidas");
    }

    @Test
    void mayChooseLibraryCopyInsteadOfGraveyardCopy() {
        TheSpearOfLeonidas libraryCopy = new TheSpearOfLeonidas();
        harness.setGraveyard(player1, List.of(new TheSpearOfLeonidas()));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        var search = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).contains(libraryCopy);
        harness.handleCardChosen(player1, search.params().cards().indexOf(libraryCopy));

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertInGraveyard(player1, "The Spear of Leonidas");
    }

    @Test
    void shufflesEvenWhenTheLibraryIsEmptyAndNoSpearExists() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertLife(player1, 18);
    }

    @Test
    void drawsOnlyOnceForMultipleLegendaryEquipmentOnOneCreature() {
        Permanent attacker = addCreatureReady(player1, new KassandraEagleBearer());
        harness.addToBattlefieldAndReturn(player1, new TheSpearOfLeonidas()).setAttachedTo(attacker.getId());
        harness.addToBattlefieldAndReturn(player1, new UmezawasJitte()).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void drawsForEachQualifyingCreatureDealingCombatDamage() {
        Permanent first = addCreatureReady(player1, new KassandraEagleBearer());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new TheSpearOfLeonidas()).setAttachedTo(first.getId());
        harness.addToBattlefieldAndReturn(player1, new UmezawasJitte()).setAttachedTo(second.getId());
        first.setAttacking(true);
        second.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
