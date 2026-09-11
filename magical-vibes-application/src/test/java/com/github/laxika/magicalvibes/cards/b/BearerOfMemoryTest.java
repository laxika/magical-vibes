package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BearerOfMemory.class, GrizzlyBears.class})
class BearerOfMemoryTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetAndGrantsTrample() {
        Permanent bearer = addReadyBearer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        addAbilityMana();

        harness.activateAbility(player1, battlefieldIndex(bearer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canTargetAnOpponentsEnchantmentCreature() {
        Permanent bearer = addReadyBearer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        addAbilityMana();

        harness.activateAbility(player1, battlefieldIndex(bearer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void trampleWearsOffAtEndOfTurnButCounterRemains() {
        Permanent bearer = addReadyBearer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        addAbilityMana();

        harness.activateAbility(player1, battlefieldIndex(bearer), null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetNonEnchantmentCreature() {
        Permanent bearer = addReadyBearer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bearer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment creature");
    }

    private Permanent addReadyBearer(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bearer = harness.addToBattlefieldAndReturn(player, new BearerOfMemory());
        bearer.setSummoningSick(false);
        return bearer;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
