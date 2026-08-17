package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TourachsGateTest extends BaseCardTest {

    @Test
    @DisplayName("Tourach's Gate enchants a land its controller controls")
    void enchantsControlledLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, java.util.List.of(new TourachsGate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Tourach's Gate");
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Sacrificing a Thrull puts three time counters on Tourach's Gate")
    void sacrificingThrullAddsTimeCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addGate(land);
        harness.addToBattlefield(player1, new ThrullChampion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Thrull Champion");
    }

    @Test
    @DisplayName("Tourach's Gate cannot sacrifice a non-Thrull creature")
    void sacrificeCostRequiresThrull() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addGate(land);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tourach's Gate removes a time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addGate(land);
        aura.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player1, "Tourach's Gate");
    }

    @Test
    @DisplayName("Tourach's Gate is sacrificed during upkeep when it has no time counters")
    void upkeepSacrificesWithoutTimeCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addGate(land);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tourach's Gate");
        harness.assertInGraveyard(player1, "Tourach's Gate");
    }

    @Test
    @DisplayName("Tapping the enchanted land boosts attacking creatures you control")
    void boostsOwnAttackers() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addGate(land);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, java.util.List.of(indexOf(attacker)));
        harness.activateAbility(player1, indexOf(aura), 1, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tourach's Gate cannot tap an already-tapped enchanted land")
    void cannotActivateWithTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addGate(land);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, java.util.List.of(indexOf(attacker)));
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addGate(Permanent land) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TourachsGate());
        aura.setAttachedTo(land.getId());
        return aura;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
