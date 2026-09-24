package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireNationTurret.class, GrizzlyBears.class})
class FireNationTurretTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat buffs up to one target creature with firebending")
    void beginningOfCombatBuffsTargetCreature() {
        harness.addToBattlefield(player1, new FireNationTurret());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIREBENDING)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIREBENDING)).isFalse();
    }

    @Test
    @DisplayName("The red ability adds a charge counter")
    void redAbilityAddsChargeCounter() {
        Permanent turret = harness.addToBattlefieldAndReturn(player1, new FireNationTurret());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(turret.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing fifty charge counters deals 50 damage to any target")
    void removesFiftyChargeCountersAndDealsDamage() {
        Permanent turret = harness.addToBattlefieldAndReturn(player1, new FireNationTurret());
        turret.setCounterCount(CounterType.CHARGE, 50);
        UUID target = player2.getId();

        harness.activateAbility(player1, 0, 1, null, target);
        harness.passBothPriorities();

        assertThat(turret.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-30);
    }

    @Test
    @DisplayName("The damage ability requires fifty charge counters")
    void damageAbilityRequiresFiftyChargeCounters() {
        Permanent turret = harness.addToBattlefieldAndReturn(player1, new FireNationTurret());
        turret.setCounterCount(CounterType.CHARGE, 49);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
