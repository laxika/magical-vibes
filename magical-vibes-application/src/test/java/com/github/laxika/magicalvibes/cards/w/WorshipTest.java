package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Worship.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class WorshipTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage can't reduce controller's life below 1 while they control a creature")
    void noncombatDamageCappedToOneWithCreature() {
        harness.addToBattlefield(player1, new Worship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 2);

        shockPlayer1();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Worship does not protect the controller when they control no creature")
    void noProtectionWithoutCreature() {
        harness.addToBattlefield(player1, new Worship());
        harness.setLife(player1, 2);

        shockPlayer1();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life loss bypasses Worship")
    void lifeLossBypassesWorship() {
        harness.addToBattlefield(player1, new Worship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 2);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 2, "test"));
        harness.runStateBasedActions();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed(PlatinumAngel.class)
    @DisplayName("Damage does not raise life from below 1")
    void damageDoesNotRaiseLifeFromBelowOne() {
        harness.addToBattlefield(player1, new Worship());
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "test"));
        shockPlayer1();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(-2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Combat damage is capped to 1 regardless of magnitude while controlling a creature")
    void combatDamageCappedToOne() {
        harness.addToBattlefield(player1, new Worship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 2);

        // player2 attacks player1 with a 3/3 — lethal (would go to -1) without Worship
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
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
