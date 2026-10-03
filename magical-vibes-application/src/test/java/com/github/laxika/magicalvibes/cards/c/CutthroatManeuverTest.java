package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutthroatManeuver.class, TravelingPhilosopher.class, Mountain.class})
class CutthroatManeuverTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Both target creatures get +1/+1 and lifelink")
    void twoTargetsGetBoostAndLifelink() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Boost and lifelink wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroTargetsAllowed() {
        Permanent untouched = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        harness.assertInGraveyard(player1, "Cutthroat Maneuver");
        assertThat(gqs.getEffectivePower(gd, untouched)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untouched)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, untouched, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingTargetGetsBothEffectsWhenOtherTargetLeaves() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();

        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void lifelinkGainsLifeForCreatureControllerRatherThanSpellController() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CutthroatManeuver()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        harness.forceActivePlayer(player2);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }
}
