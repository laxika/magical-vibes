package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OmenportVigilante;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TakeUpTheShield.class, OmenportVigilante.class, Plains.class})
class TakeUpTheShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target and grants lifelink and indestructible")
    void putsCounterAndGrantsKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castTakeUpTheShield(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Temporary keyword grants expire at end of turn but the counter remains")
    void keywordsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castTakeUpTheShield(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OmenportVigilante());

        castTakeUpTheShield(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void doesNotAffectReplacementPermanentWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(replacement.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(replacement.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Take Up the Shield");
    }

    @Test
    void indestructibleProtectsTargetFromLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castTakeUpTheShield(target);

        target.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Omenport Vigilante");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Omenport Vigilante");
    }

    @Test
    void grantedLifelinkGainsLifeFromCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castTakeUpTheShield(attacker);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castTakeUpTheShield(Permanent target) {
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
