package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousAltisaur.class, RaptorCompanion.class, GrizzlyBears.class, Forest.class, TurnToFrog.class})
class CuriousAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Dinosaur you control deals combat damage to a player")
    void drawsWhenDinosaurDealsCombatDamage() {
        addCuriousAltisaur();
        Permanent dinosaur = addCreatureReady(player1, new RaptorCompanion());
        dinosaur.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a non-Dinosaur creature deals combat damage")
    void ignoresNonDinosaurCombatDamage() {
        addCuriousAltisaur();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Draws once for each Dinosaur that deals combat damage")
    void drawsForEachDinosaur() {
        addCuriousAltisaur();
        Permanent firstDinosaur = addCreatureReady(player1, new RaptorCompanion());
        firstDinosaur.setAttacking(true);
        Permanent secondDinosaur = addCreatureReady(player1, new RaptorCompanion());
        secondDinosaur.setAttacking(true);
        Card firstTopCard = new Forest();
        Card secondTopCard = new Forest();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstTopCard, secondTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Curious Altisaur triggers for its own combat damage")
    void drawsForItsOwnCombatDamage() {
        Permanent altisaur = addCreatureReady(player1, new CuriousAltisaur());
        altisaur.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opposing Dinosaur does not trigger your Altisaur")
    void ignoresOpposingDinosaurCombatDamage() {
        addCuriousAltisaur();
        Permanent attacker = addCreatureReady(player2, new CuriousAltisaur());
        attacker.setAttacking(true);
        Card ownTopCard = new Forest();
        Card opposingTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingTopCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Altisaur triggers independently for the same Dinosaur")
    void multipleAltisaursEachDraw() {
        addCuriousAltisaur();
        Permanent attacker = addCreatureReady(player1, new CuriousAltisaur());
        attacker.setAttacking(true);
        Card firstCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void blockedDinosaurDoesNotDraw() {
        Permanent attacker = addCreatureReady(player1, new CuriousAltisaur());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CuriousAltisaur());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Card ownTopCard = new Forest();
        Card opposingTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTopCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A queued draw trigger resolves after Altisaur leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        Permanent attacker = addCreatureReady(player1, new CuriousAltisaur());
        attacker.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Altisaur cannot trigger after losing all abilities")
    void doesNotDrawAfterLosingAllAbilities() {
        Permanent altisaur = addCuriousAltisaur();
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, altisaur.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addCuriousAltisaur() {
        return harness.addToBattlefieldAndReturn(player1, new CuriousAltisaur());
    }
}
