package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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

@CardUsed({UncagedFury.class, WalkingCorpse.class, Murder.class, Swamp.class})
class UncagedFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives +1/+1 and double strike to the target creature")
    void resolvingBoostsAndGrantsDoubleStrike() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and double strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void canBoostAnOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void repeatedCastsStackTheBoostAndExpireTogether() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new UncagedFury(), new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void removedTargetDoesNotRedirectTheEffectsToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WalkingCorpse)
                .anyMatch(card -> card instanceof UncagedFury);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostedUnblockedCreatureDealsDamageInBothCombatDamageSteps() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UncagedFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 14);
    }
}
