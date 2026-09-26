package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DemonicTorment;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.l.LadyEvangela;
import com.github.laxika.magicalvibes.cards.l.LostSoul;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlingDervish.class, FireWhip.class, DurkwoodBoars.class, LostSoul.class,
        DemonicTorment.class, LadyEvangela.class})
class WhirlingDervishTest extends BaseCardTest {

    @Test
    void damageToAnOpponentsCreatureDoesNotQualify() {
        Permanent dervish = addDervish(player1);
        Permanent victim = addCreatureReady(player2, new DurkwoodBoars());

        dealOneDamageWithFireWhip(dervish, victim.getId());
        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        Player activePlayer = activePlayerId.equals(player1.getId()) ? player1 : player2;
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private void dealOneDamageWithFireWhip(Permanent dervish, UUID targetId) {
        Permanent fireWhip = new Permanent(new FireWhip());
        fireWhip.setAttachedTo(dervish.getId());
        gd.playerBattlefields.get(player1.getId()).add(fireWhip);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter at end step after dealing damage to an opponent")
    void getsCounterAfterDealingDamage() {
        Permanent dervish = addDervish(player1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when it dealt no damage this turn")
    void noCounterWithoutDamage() {
        Permanent dervish = addDervish(player1);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt only to its own controller does not qualify")
    void noCounterWhenDamageNotToOpponent() {
        Permanent dervish = addDervish(player1);

        dealOneDamageWithFireWhip(dervish, player1.getId());
        harness.assertLife(player1, 19);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's, when it dealt damage to an opponent")
    void triggersOnEachEndStep() {
        Permanent dervish = addDervish(player1);
        dealOneDamageWithFireWhip(dervish, player2.getId());

        // It is player2's (the opponent's) turn.
        advanceToEndStepAndResolve(player2.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter after dealing noncombat damage to an opponent")
    void getsCounterAfterDealingNoncombatDamage() {
        Permanent dervish = addDervish(player1);
        dealOneDamageWithFireWhip(dervish, player2.getId());

        harness.assertLife(player2, 19);
        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent dervish = addDervish(player1);
        dervish.setAttacking(true);
        addCreatureReady(player2, new LostSoul());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents a black Aura from enchanting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent dervish = addDervish(player1);

        harness.setHand(player2, List.of(new DemonicTorment()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, dervish.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black prevents a black ability from targeting it")
    void protectionFromBlackPreventsBlackAbilityTargeting() {
        Permanent dervish = addDervish(player1);
        addCreatureReady(player2, new LadyEvangela());

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, dervish.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new LostSoul());
        attacker.setAttacking(true);
        Permanent dervish = addDervish(player1);
        dervish.setBlocking(true);
        dervish.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dervish);
        assertThat(dervish.getMarkedDamage()).isZero();
    }

    private Permanent addDervish(Player player) {
        return addCreatureReady(player, new WhirlingDervish());
    }
}
