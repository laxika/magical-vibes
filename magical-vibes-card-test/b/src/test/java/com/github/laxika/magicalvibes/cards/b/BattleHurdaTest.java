package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BattleHurda.class, WalkingAtlas.class})
class BattleHurdaTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked first striker deals damage only once")
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new BattleHurda());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("First strike deals damage before a smaller blocker")
    void firstStrikeDealsDamageBeforeSmallerBlocker() {
        Permanent attacker = addCreatureReady(player1, new BattleHurda());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WalkingAtlas());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Battle Hurda");
        harness.assertInGraveyard(player2, "Walking Atlas");
    }

    @Test
    @DisplayName("First strike also kills an attacker before it deals damage")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new WalkingAtlas());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BattleHurda());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Walking Atlas");
        harness.assertOnBattlefield(player2, "Battle Hurda");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing first strikers deal lethal damage simultaneously")
    void opposingFirstStrikersTrade() {
        Permanent attacker = addCreatureReady(player1, new BattleHurda());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BattleHurda());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Battle Hurda");
        harness.assertInGraveyard(player2, "Battle Hurda");
    }
}
