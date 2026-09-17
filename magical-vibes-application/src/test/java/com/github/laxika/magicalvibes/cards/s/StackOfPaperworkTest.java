package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StackOfPaperwork.class, GrizzlyBears.class})
class StackOfPaperworkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersAndDrawsCard() {
        GrizzlyBears cardToDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.setHand(player1, List.of(new StackOfPaperwork()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
    }

    @Test
    @DisplayName("Puts assigned combat damage on the stack before dealing it")
    void putsCombatDamageOnStack() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
