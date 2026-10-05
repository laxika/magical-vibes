package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.e.EvolutionCharm;
import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ovinize.class, AvenRiftwatcher.class, EvolutionCharm.class, GaeasAnthem.class, ProdigalPyromancer.class})
class OvinizeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature 0/1 and removes its abilities")
    void makesTargetZeroOneWithoutAbilities() {
        Permanent riftwatcher = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());
        assertThat(gqs.hasKeyword(gd, riftwatcher, Keyword.FLYING)).isTrue();

        castOvinize(riftwatcher.getId());

        assertThat(riftwatcher.getEffectivePower()).isZero();
        assertThat(riftwatcher.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, riftwatcher, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sets base power and toughness without removing other modifiers")
    void preservesOtherPowerAndToughnessModifiers() {
        Permanent riftwatcher = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(riftwatcher.getEffectivePower()).isEqualTo(3);
        assertThat(riftwatcher.getEffectiveToughness()).isEqualTo(4);

        castOvinize(riftwatcher.getId());

        assertThat(riftwatcher.getEffectivePower()).isEqualTo(1);
        assertThat(riftwatcher.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes activated abilities as well as keyword abilities")
    void removesActivatedAbilities() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        castOvinize(pyromancer.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent riftwatcher = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());
        castOvinize(riftwatcher.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(riftwatcher.getEffectivePower()).isEqualTo(2);
        assertThat(riftwatcher.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, riftwatcher, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());
        harness.addToBattlefield(player1, new AvenRiftwatcher());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID anthemId = anthem.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, anthemId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("External static bonuses still apply to the affected creature")
    void preservesAnthemBonus() {
        harness.addToBattlefield(player2, new GaeasAnthem());
        Permanent riftwatcher = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());

        castOvinize(riftwatcher.getId());

        assertThat(gqs.getEffectivePower(gd, riftwatcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, riftwatcher)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, riftwatcher, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An ability granted after Ovinize resolves survives")
    void preservesLaterGrantedFlying() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        castOvinize(pyromancer.getId());

        grantFlying(pyromancer.getId());

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pyromancer)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pyromancer)).isEqualTo(1);
    }

    @Test
    @DisplayName("An ability granted before Ovinize resolves is removed")
    void removesEarlierGrantedFlying() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        grantFlying(pyromancer.getId());
        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.FLYING)).isTrue();

        castOvinize(pyromancer.getId());

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing abilities suppresses the creature's leaves-the-battlefield trigger")
    void suppressesLeavesBattlefieldTrigger() {
        Permanent riftwatcher = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setLife(player2, 20);
        castOvinize(riftwatcher.getId());

        harness.activateAbility(player1, 0, null, riftwatcher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Riftwatcher");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing abilities does not counter an ability already on the stack")
    void preservesAlreadyActivatedAbility() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());

        castOvinize(pyromancer.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void grantFlying(UUID targetId) {
        harness.setHand(player1, List.of(new EvolutionCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();
    }

    private void castOvinize(UUID targetId) {
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
