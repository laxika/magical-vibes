package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({StrengthInNumbers.class, AshcoatBear.class, Mountain.class, Snapback.class})
class StrengthInNumbersTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +X/+X and trample based on attacking creatures")
    void boostsByAttackingCreatureCountAndGrantsTrample() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The pump and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no attackers, a nonattacking creature still gains trample")
    void grantsTrampleWithoutAttackers() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counts opponents' attackers and can target an opponent's nonattacking creature")
    void countsOpponentsAttackersAndTargetsNonattacker() {
        addCreatureReady(player2, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        addCreatureReady(player1, new AshcoatBear());
        declareAttackers(player2, List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counts attackers at resolution and keeps that bonus when an attacker later leaves")
    void countsAtResolutionAndLocksInBonus() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        Permanent otherAttacker = addCreatureReady(player1, new AshcoatBear());
        Permanent remainingAttacker = addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());
        declareAttackers(List.of(0, 1, 2));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.setHand(player2, List.of(new Snapback(), new Snapback()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, otherAttacker.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Ashcoat Bear");
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.castAndResolveInstant(player2, 0, remainingAttacker.getId());


        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant a bonus or trample if its only target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsEffects() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new StrengthInNumbers()));
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Strength in Numbers");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInHand(player1, "Ashcoat Bear");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
