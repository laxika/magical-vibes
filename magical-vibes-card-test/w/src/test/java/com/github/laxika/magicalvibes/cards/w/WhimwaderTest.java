package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.k.KithkinShielddare;
import com.github.laxika.magicalvibes.cards.s.Somnomancer;
import com.github.laxika.magicalvibes.cards.s.SunkenRuins;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whimwader.class, BriarberryCohort.class, KithkinShielddare.class,
        Somnomancer.class, SunkenRuins.class, ThoughtReflection.class})
class WhimwaderTest extends BaseCardTest {

    private Permanent addWhimwaderReadyToAttack() {
        return addCreatureReady(player1, new Whimwader());
    }

    @Test
    @DisplayName("Whimwader can attack when defending player controls a blue permanent")
    void canAttackWhenDefenderControlsBluePermanent() {
        harness.addToBattlefield(player2, new BriarberryCohort());
        addWhimwaderReadyToAttack();

        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Whimwader cannot attack when defending player controls only a non-blue permanent")
    void cannotAttackWhenDefenderControlsOnlyNonBluePermanent() {
        harness.addToBattlefield(player2, new KithkinShielddare());
        addWhimwaderReadyToAttack();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Whimwader cannot attack when defending player controls no permanents")
    void cannotAttackWhenDefenderControlsNothing() {
        addWhimwaderReadyToAttack();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Whimwader cannot attack when only its controller controls a blue permanent")
    void cannotAttackWhenOnlyAttackerControlsBluePermanent() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        addWhimwaderReadyToAttack();

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blue noncreature permanent allows Whimwader to attack")
    void canAttackWhenDefenderControlsBlueEnchantment() {
        harness.addToBattlefield(player2, new ThoughtReflection());
        Permanent attacker = addWhimwaderReadyToAttack();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A multicolored blue permanent allows Whimwader to attack")
    void canAttackWhenDefenderControlsMulticoloredBluePermanent() {
        harness.addToBattlefield(player2, new Somnomancer());
        Permanent attacker = addWhimwaderReadyToAttack();

        declareAttackers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A colorless land producing blue mana does not allow Whimwader to attack")
    void cannotAttackWhenDefenderControlsOnlyBlueManaProducingLand() {
        harness.addToBattlefield(player2, new SunkenRuins());
        addWhimwaderReadyToAttack();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
