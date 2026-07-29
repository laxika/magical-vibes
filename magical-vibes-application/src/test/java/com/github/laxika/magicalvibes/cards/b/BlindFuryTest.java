package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlindFuryTest extends BaseCardTest {

    private void castBlindFury() {
        harness.setHand(player1, List.of(new BlindFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(Card card) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blocker);
        return blocker;
    }

    private void resolveOpponentCombat(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        if (blocker != null) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage dealt to a blocking creature is doubled")
    void doublesCombatDamageToBlocker() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        Permanent blocker = addBlocker(new GiantSpider());
        castBlindFury();

        resolveOpponentCombat(attacker, blocker);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Without Blind Fury a 2/2 attacker does not kill a 2/4 blocker")
    void blockerSurvivesWithoutBlindFury() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        Permanent blocker = addBlocker(new GiantSpider());

        resolveOpponentCombat(attacker, blocker);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Combat damage dealt to a player is not doubled")
    void doesNotDoubleDamageToPlayers() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(new GrizzlyBears());
        castBlindFury();

        resolveOpponentCombat(attacker, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("All creatures lose trample, so a blocked trampler assigns nothing to the player")
    void stripsTrample() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(new AvatarOfMight());
        Permanent blocker = addBlocker(new GrizzlyBears());
        castBlindFury();

        resolveOpponentCombat(attacker, blocker);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Giant Spider"));
    }
}
