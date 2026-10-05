package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProvokeTheTrolls.class, AirElemental.class, HealingSalve.class, Forest.class, TyvarKell.class})
class ProvokeTheTrollsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and gives it +5/+0")
    void damagesAndBoostsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(9);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals 3 damage to a player without a creature boost")
    void damagesPlayer() {
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not boost a creature when all damage is prevented")
    void doesNotBoostWhenDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a land that is not a creature")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can boost your own creature without boosting creatures damaged earlier")
    void boostsOnlyCreatureDamagedByThisResolution() {
        Permanent earlierTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls(), new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, earlierTarget.getId());
        harness.castAndResolveInstant(player1, 0, ownTarget.getId());

        assertThat(earlierTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(earlierTarget.getEffectivePower()).isEqualTo(9);
        assertThat(ownTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(ownTarget.getEffectivePower()).isEqualTo(9);
        assertThat(ownTarget.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The power boost does not save a creature from lethal damage")
    void repeatedDamageKillsCreatureDespitePowerBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls(), new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Repeated damage gives a surviving creature a boost for each spell")
    void repeatedBoostsAccumulate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new ProvokeTheTrolls(), new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(target.getEffectivePower()).isEqualTo(17);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Earlier damage does not qualify a creature for a prevented spell's boost")
    void earlierDamageDoesNotCauseBoostWhenNewDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ProvokeTheTrolls(), new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(9);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals damage to a planeswalker without boosting it")
    void damagesPlaneswalkerWithoutBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Tyvar Kell");
    }

    @Test
    @DisplayName("Can target its controller")
    void damagesController() {
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
