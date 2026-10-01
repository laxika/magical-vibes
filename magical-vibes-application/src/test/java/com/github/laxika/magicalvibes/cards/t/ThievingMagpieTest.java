package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CacklingFiend;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HermeticStudy.class, ThievingMagpie.class})
class ThievingMagpieTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to an opponent draws a card")
    void combatDamageToOpponentDrawsCard() {
        prepareDrawState();
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ThievingMagpie());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A blocked Magpie deals no damage to an opponent and draws no card")
    void blockedMagpieDoesNotDraw() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new ThievingMagpie());
        Permanent blocker = addCreatureReady(player2, new ThievingMagpie());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({ThievingMagpie.class, CacklingFiend.class})
    @DisplayName("Flying prevents a nonflying creature from blocking the Magpie")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        prepareDrawState();
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ThievingMagpie());
        addCreatureReady(player2, new CacklingFiend());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({ThievingMagpie.class, GiantSpider.class})
    @DisplayName("Reach allows a Giant Spider to block the flying Magpie")
    void reachAllowsGiantSpiderToBlock() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new ThievingMagpie());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage dealt by the Magpie to an opponent draws a card")
    void noncombatDamageToOpponentDrawsCard() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent magpie = addCreatureReady(player1, new ThievingMagpie());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(magpie.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt by the Magpie to its controller does not draw a card")
    void damageToControllerDoesNotDraw() {
        prepareDrawState();
        harness.setLife(player1, 20);

        Permanent magpie = addCreatureReady(player1, new ThievingMagpie());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(magpie.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void prepareDrawState() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThievingMagpie()));
    }

    @Test
    @DisplayName("Draws a card when it deals combat damage to an opponent")
    void drawsOnCombatDamageToOpponent() {
        prepareDrawState();
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThievingMagpie());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Thieving Magpie");
    }

    @Test
    @DisplayName("Each Magpie that deals damage draws a card")
    void eachMagpieDrawsForItsDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThievingMagpie(), new ThievingMagpie()));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThievingMagpie());
        addCreatureReady(player1, new ThievingMagpie());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches the opponent")
    void doesNotDrawWhenBlocked() {
        prepareDrawState();
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ThievingMagpie());
        Permanent blocker = addCreatureReady(player2, new ThievingMagpie());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
