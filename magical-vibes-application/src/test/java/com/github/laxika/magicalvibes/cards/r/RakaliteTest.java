package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakaliteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage to a targeted player")
    void preventsNextDamageToTargetPlayer() {
        Permanent rakalite = addReadyRakalite(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rakalite);
    }

    @Test
    @DisplayName("Returns itself at the next end step after resolving the ability")
    void returnsItselfAtNextEndStep() {
        Permanent rakalite = addReadyRakalite(player1);
        Card card = rakalite.getCard();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rakalite);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    private Permanent addReadyRakalite(Player player) {
        Permanent rakalite = new Permanent(new Rakalite());
        rakalite.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(rakalite);
        return rakalite;
    }
}
