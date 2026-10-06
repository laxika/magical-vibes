package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HinterlandLogger;
import com.github.laxika.magicalvibes.cards.h.HowlingChorus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrillHowler.class, HowlingChorus.class, HinterlandLogger.class})
class ShrillHowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new HinterlandLogger());
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new ShrillHowler());
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{5}{G} transforms into Howling Chorus")
    void transformAbilityFlipsToHowlingChorus() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Howling Chorus creates an Eldrazi Horror token on combat damage to a player")
    void backFaceCreatesTokenOnCombatDamage() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        howler.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Eldrazi Horror")).isEqualTo(1);
        assertThat(countPermanents(player2, "Eldrazi Horror")).isZero();
        Permanent token = findPermanent(player1, "Eldrazi Horror");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI, CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Howling Chorus also can't be blocked by lower-power creatures")
    void backFaceCannotBeBlockedByLowerPower() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new HinterlandLogger());
        howler.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("A smaller printed creature can block after its power is increased")
    void blockingUsesEffectivePower() {
        Permanent blocker = addCreatureReady(player2, new HinterlandLogger());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Increasing the attacker's power makes an equal printed power blocker illegal")
    void blockingUsesAttackersEffectivePower() {
        Permanent blocker = addCreatureReady(player2, new ShrillHowler());
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        howler.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The front face deals combat damage without creating a token")
    void frontFaceDoesNotCreateToken() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Eldrazi Horror")).isZero();
    }

    @Test
    @DisplayName("Howling Chorus can be blocked by equal power and creates no token for creature damage")
    void backFaceBlockedCombatDoesNotCreateToken() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new ShrillHowler());
        howler.setAttacking(true);
        harness.setLife(player2, 20);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Eldrazi Horror")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(howler);
    }
}
