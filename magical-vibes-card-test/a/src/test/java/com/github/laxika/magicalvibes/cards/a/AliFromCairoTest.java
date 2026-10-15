package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AliFromCairo.class, HillGiant.class, Shock.class, Humble.class})
class AliFromCairoTest extends BaseCardTest {

    @Test
    void noncombatDamageCannotReduceLifeBelowOne() {
        harness.addToBattlefield(player1, new AliFromCairo());
        harness.setLife(player1, 2);

        shockPlayer1();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void combatDamageCannotReduceLifeBelowOne() {
        harness.addToBattlefield(player1, new AliFromCairo());
        harness.setLife(player1, 2);

        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void lifeLossIsNotPrevented() {
        harness.addToBattlefield(player1, new AliFromCairo());
        harness.setLife(player1, 1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "test"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
    }

    @Test
    void damageAtOneLifeStillCountsAsDamage() {
        harness.addToBattlefield(player1, new AliFromCairo());
        harness.setLife(player1, 1);

        shockPlayer1();

        harness.assertLife(player1, 1);
        assertThat(gd.damageDealtToPlayersThisTurn.get(player1.getId())).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void nonlethalDamageReducesLifeNormally() {
        harness.addToBattlefield(player1, new AliFromCairo());
        harness.setLife(player1, 5);

        shockPlayer1();

        harness.assertLife(player1, 3);
    }

    @Test
    void doesNotProtectOpponent() {
        harness.addToBattlefield(player2, new AliFromCairo());
        harness.setLife(player1, 2);

        shockPlayer1();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void losingAbilitiesRemovesDamageProtection() {
        Permanent ali = harness.addToBattlefieldAndReturn(player1, new AliFromCairo());
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, ali.getId());

        shockPlayer1();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void protectionEndsWhenAliDies() {
        Permanent ali = harness.addToBattlefieldAndReturn(player1, new AliFromCairo());
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, ali.getId());
        harness.assertInGraveyard(player1, "Ali from Cairo");

        shockPlayer1();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void shockPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
    }
}
