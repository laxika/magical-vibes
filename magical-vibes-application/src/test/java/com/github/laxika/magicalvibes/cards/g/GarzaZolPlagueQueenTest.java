package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.c.CoverOfWinter;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.cards.w.WallOfShards;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarzaZolPlagueQueen.class, BorealDruid.class, BorealGriffin.class, CoverOfWinter.class,
        WallOfShards.class, SuddenSpoiling.class})
class GarzaZolPlagueQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature it damaged dies")
    void gainsCounterWhenDamagedCreatureDies() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorealGriffin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain a counter when the damaged creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        BorealGriffin card = new BorealGriffin();
        card.setToughness(6);
        Permanent blocker = addCreatureReady(player2, card);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gains a counter when a creature it damaged earlier this turn dies later")
    void gainsCounterWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        BorealGriffin card = new BorealGriffin();
        card.setToughness(6);
        Permanent blocker = addCreatureReady(player2, card);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, blocker));
        harness.passBothPriorities();

        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain a counter when an undamaged creature dies")
    void noCounterWhenUndamagedCreatureDies() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        Permanent creature = addCreatureReady(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not gain a counter when its combat damage is prevented")
    void noCounterWhenCombatDamageIsPrevented() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorealGriffin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent cover = harness.addToBattlefieldAndReturn(player2, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, blocker));
        harness.passBothPriorities();

        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May draw a card when it deals combat damage to a player")
    void mayDrawOnCombatDamage() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new BorealDruid())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline the card draw")
    void mayDeclineDraw() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new BorealDruid())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Prevented combat damage to a player does not trigger the optional draw")
    void noDrawWhenPlayerDamageIsPrevented() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        Permanent cover = harness.addToBattlefieldAndReturn(player2, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 5);
        harness.setLibrary(player1, List.of(new BorealDruid()));
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The optional draw resolves even if Garza Zol leaves the battlefield")
    void drawResolvesAfterSourceLeavesBattlefield() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        harness.setLibrary(player1, List.of(new BorealDruid()));
        harness.setHand(player1, List.of());

        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, garza));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(garza);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities before a damaged creature dies prevents the counter trigger")
    void noCounterWhenAbilityIsLostBeforeDamagedCreatureDies() {
        Permanent garza = addCreatureReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfShards());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);

        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(garza);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, blocker));
        harness.passBothPriorities();

        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
