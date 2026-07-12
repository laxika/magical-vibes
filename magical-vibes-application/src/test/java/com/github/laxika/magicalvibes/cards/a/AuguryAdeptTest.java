package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuguryAdeptTest extends BaseCardTest {

    private Permanent addAttackingAdept() {
        Permanent perm = new Permanent(new AuguryAdept());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage
        harness.passBothPriorities(); // resolve the triggered ability
    }

    @Test
    @DisplayName("Combat damage to a player reveals top card into hand and gains life equal to its mana value")
    void revealsAndGainsLife() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new GrizzlyBears(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // took 2 combat damage
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22); // gained MV 2 life
    }

    @Test
    @DisplayName("Revealing a land (mana value 0) still puts it into hand but gains no life")
    void revealingLandGainsNoLife() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when blocked and dealing no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // SerraAngel (4/4) blocks the 2/2 Adept — no damage reaches player2.
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
