package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalcatorsWatcher.class, Forest.class, Shock.class, CrawlingChorus.class})
class MalcatorsWatcherTest extends BaseCardTest {

    @Test
    void drawsCardWhenItDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new MalcatorsWatcher());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Malcator's Watcher");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertInGraveyard(player1, "Malcator's Watcher");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void simultaneousDeathsEachDrawExactlyOneCard() {
        MalcatorsWatcher firstDraw = new MalcatorsWatcher();
        MalcatorsWatcher secondDraw = new MalcatorsWatcher();
        MalcatorsWatcher remainingCard = new MalcatorsWatcher();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remainingCard));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MalcatorsWatcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MalcatorsWatcher());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void deathDrawsForControllerRatherThanOwner() {
        MalcatorsWatcher watcher = new MalcatorsWatcher();
        watcher.setOwnerId(player1.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, watcher);
        MalcatorsWatcher drawnCard = new MalcatorsWatcher();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        permanent.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Malcator's Watcher");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    void sacrificeAlsoTriggersDraw() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new MalcatorsWatcher());
        MalcatorsWatcher drawnCard = new MalcatorsWatcher();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, watcher));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Malcator's Watcher");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void exileDoesNotTriggerDraw() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new MalcatorsWatcher());
        MalcatorsWatcher libraryCard = new MalcatorsWatcher();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, watcher));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().equals(watcher.getCard())
                && entry.ownerId().equals(player1.getId()));
    }

    @Test
    void attackingDoesNotTapWatcher() {
        Permanent watcher = addCreatureReady(player1, new MalcatorsWatcher());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(watcher.isAttacking()).isTrue();
        assertThat(watcher.isTapped()).isFalse();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlockingButAllowsFlyingBlocker() {
        Permanent attacker = addCreatureReady(player1, new MalcatorsWatcher());
        Permanent groundBlocker = addCreatureReady(player2, new CrawlingChorus());
        Permanent flyingBlocker = addCreatureReady(player2, new MalcatorsWatcher());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, groundBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
