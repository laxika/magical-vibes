package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.s.SkeletalVampire;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.cards.s.StreetbreakerWurm;
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

@CardUsed({CrashLanding.class, DaggerclawImp.class, SkeletalVampire.class, StompingGround.class, StreetbreakerWurm.class})
class CrashLandingTest extends BaseCardTest {

    @Test
    void removesFlyingAndDealsDamageEqualToForestsYouControl() {
        harness.addToBattlefield(player1, new StompingGround());
        harness.addToBattlefield(player1, new StompingGround());
        harness.addToBattlefield(player2, new StompingGround());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkeletalVampire());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Skeletal Vampire");
    }

    @Test
    void removesFlyingButDealsNoDamageWhenYouControlNoForests() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void flyingRemovalWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StreetbreakerWurm());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsForestsAtResolutionAndCanKillYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DaggerclawImp());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new StompingGround());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daggerclaw Imp");
        harness.assertInGraveyard(player1, "Daggerclaw Imp");
    }

    @Test
    void doesNotDealDamageIfTargetLosesFlyingBeforeResolution() {
        harness.addToBattlefield(player1, new StompingGround());
        harness.addToBattlefield(player1, new StompingGround());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkeletalVampire());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.setHand(player2, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Skeletal Vampire");
        harness.assertInGraveyard(player1, "Crash Landing");
        harness.assertInGraveyard(player2, "Crash Landing");
    }

    @Test
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StompingGround());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
