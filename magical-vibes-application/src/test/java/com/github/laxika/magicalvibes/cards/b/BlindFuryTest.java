package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({BlindFury.class, AvatarOfMight.class, GiantSpider.class, GrizzlyBears.class})
class BlindFuryTest extends BaseCardTest {

    private void castBlindFury() {
        harness.castFromHand(player1, new BlindFury(), "{2}{R}{R}");
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Card card) {
        return addCreatureReady(player2, card);
    }

    private Permanent addBlocker(Card card) {
        return addCreatureReady(player1, card);
    }

    private void resolveOpponentCombat(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        if (blocker != null) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        }
        resolveCombat(player2);
    }

    @Test
    @DisplayName("Combat damage dealt to a blocking creature is doubled")
    void doublesCombatDamageToBlocker() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        Permanent blocker = addBlocker(new GiantSpider());
        castBlindFury();

        resolveOpponentCombat(attacker, blocker);

        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Combat damage dealt by a blocking creature is also doubled")
    void doublesCombatDamageToAttackingCreature() {
        Permanent attacker = addAttacker(new GiantSpider());
        Permanent blocker = addBlocker(new GiantSpider());
        castBlindFury();

        resolveOpponentCombat(attacker, blocker);

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Without Blind Fury a 2/2 attacker does not kill a 2/4 blocker")
    void blockerSurvivesWithoutBlindFury() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        Permanent blocker = addBlocker(new GiantSpider());

        resolveOpponentCombat(attacker, blocker);

        harness.assertNotInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Combat damage dealt to a player is not doubled")
    void doesNotDoubleDamageToPlayers() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(new GrizzlyBears());
        castBlindFury();

        resolveOpponentCombat(attacker, null);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("All creatures lose trample, so a blocked trampler assigns nothing to the player")
    void stripsTrample() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(new AvatarOfMight());
        Permanent blocker = addBlocker(new GrizzlyBears());
        castBlindFury();

        resolveOpponentCombat(attacker, blocker);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damage doubling wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        Permanent blocker = addBlocker(new GiantSpider());
        castBlindFury();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        resolveOpponentCombat(attacker, blocker);

        harness.assertNotInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Trample returns when Blind Fury expires at end of turn")
    void trampleReturnsAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(new AvatarOfMight());
        Permanent blocker = addBlocker(new GrizzlyBears());
        castBlindFury();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        resolveOpponentCombat(attacker, blocker);

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
