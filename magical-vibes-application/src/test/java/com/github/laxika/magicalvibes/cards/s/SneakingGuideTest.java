package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MightOfMurasa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SneakingGuide.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class, Mountain.class, MightOfMurasa.class})
class SneakingGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability makes target creature with power 2 or less unblockable")
    void resolvingMakesTargetUnblockable() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureWithEffectivePowerGreaterThanTwo() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetMustStillHavePowerTwoOrLessWhenAbilityResolves() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void unblockableResetsAtEndOfTurn() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetedCreatureCannotBeBlocked() {
        addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, targetIndex))))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void activatingOnItselfPaysManaAndTapCosts() {
        Permanent guide = addCreatureReady(player1, new SneakingGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guide.getId());

        assertThat(guide.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(guide.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(guide.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        Permanent guide = addCreatureReady(player1, new SneakingGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent guide = addCreatureReady(player1, new SneakingGuide());
        guide.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new SneakingGuide());
        guide.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainsUnblockableWhenPowerIncreasesAfterResolution() {
        Permanent guide = addCreatureReady(player1, new SneakingGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, guide.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, guide.getId());

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(4);
        assertThat(guide.isCantBeBlocked()).isTrue();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent guide = addCreatureReady(player1, new SneakingGuide());
        Permanent target = addCreatureReady(player2, new SneakingGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(guide);
        gd.playerGraveyards.get(player1.getId()).add(guide.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }
}
