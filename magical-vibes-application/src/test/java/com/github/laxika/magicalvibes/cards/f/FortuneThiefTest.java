package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.r.RiftBolt;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortuneThief.class, HavenwoodWurm.class, RiftBolt.class, PlatinumAngel.class})
class FortuneThiefTest extends BaseCardTest {

    @Test
    void damageAboveTheFloorStillReducesLifeNormally() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.setLife(player1, 5);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotProtectTheOpponent() {
        harness.addToBattlefield(player2, new FortuneThief());
        harness.setLife(player1, 2);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed(PlatinumAngel.class)
    void damageDoesNotRaiseLifeWhenAlreadyBelowOne() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, -2);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-5);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void noncombatDamageCannotReduceLifeBelowOne() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.setLife(player1, 2);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void damageLeavesLifeAtOneWhenAlreadyAtTheFloor() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.setLife(player1, 1);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void combatDamageCannotReduceLifeBelowOne() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.setLife(player1, 2);

        Permanent attacker = addCreatureReady(player2, new HavenwoodWurm());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void morphsFaceDownAndProtectionAppliesAfterTurningFaceUp() {
        Permanent fortuneThief = castFortuneThiefFaceDown();
        assertThat(fortuneThief.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fortuneThief));
        harness.passBothPriorities();

        assertThat(fortuneThief.isFaceDown()).isFalse();

        harness.setLife(player1, 2);
        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void faceDownFortuneThiefDoesNotHaveItsLifeFloorAbility() {
        castFortuneThiefFaceDown();
        harness.setLife(player1, 2);

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void protectionEndsWhenFortuneThiefDies() {
        harness.addToBattlefield(player1, new FortuneThief());
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RiftBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player2, 0, findPermanent(player1, "Fortune Thief").getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        dealNoncombatDamageToPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent castFortuneThiefFaceDown() {
        harness.setHand(player1, List.of(new FortuneThief()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Fortune Thief");
    }

    private void dealNoncombatDamageToPlayer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RiftBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
    }
}
