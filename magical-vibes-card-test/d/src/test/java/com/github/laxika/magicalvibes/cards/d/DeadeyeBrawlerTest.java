package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadeyeBrawler.class, Forest.class, GrizzlyBears.class})
class DeadeyeBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("With the city's blessing, combat damage draws a card")
    void drawsWithCityBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Without the city's blessing, combat damage does not draw a card")
    void doesNotDrawWithoutCityBlessing() {
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Entering as the tenth permanent grants the city's blessing")
    void enteringAsTenthPermanentGrantsCityBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new DeadeyeBrawler(), "{2}{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Combat damage without the blessing creates no draw trigger")
    void gainingBlessingAfterDamageDoesNotCreateDrawTrigger() {
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);
        brawler.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        gd.playersWithCityBlessing.add(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Blocked combat kills a tougher blocker with deathtouch and draws nothing")
    void blockedCombatDoesNotDraw() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);
        brawler.setAttackTarget(player2.getId());
        addCreatureReady(player2, new DeadeyeBrawler());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Deadeye Brawler");
        harness.assertInGraveyard(player2, "Deadeye Brawler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The blessing persists below ten permanents and still enables drawing")
    void blessingPersistsBelowTenPermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new DeadeyeBrawler());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != brawler);
        harness.runStateBasedActions();
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        brawler.setSummoningSick(false);
        brawler.setAttacking(true);
        brawler.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }
}

