package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ButchersGlee.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class})
class ButchersGleeTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureGrantsLifelinkAndRegeneration() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bear);

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesTargetFromLethalDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bear);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    void boostAndLifelinkWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bear);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ButchersGlee()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void lifelinkGainsLifeForOpposingCreaturesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castResolve(bear);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        bear.setSummoningSick(false);
        bear.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
    }

    @Test
    void regenerationOnlyTapsAndRemovesDamageWhenShieldIsConsumed() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setMarkedDamage(1);
        castResolve(bear);

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(bear.getRegenerationShield()).isZero();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotResolveIfTargetDiesInResponse() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ButchersGlee()));
        addMana();
        harness.castInstant(player1, 0, bear.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Butcher's Glee");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bear.getRegenerationShield()).isZero();
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new ButchersGlee()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
