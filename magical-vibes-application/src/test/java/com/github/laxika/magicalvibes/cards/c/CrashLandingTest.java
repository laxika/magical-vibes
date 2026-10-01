package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
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

@CardUsed({CrashLanding.class, DaggerclawImp.class, StompingGround.class, StreetbreakerWurm.class})
class CrashLandingTest extends BaseCardTest {

    @Test
    void removesFlyingAndDealsDamageEqualToForestsYouControl() {
        harness.addToBattlefield(player1, new StompingGround());
        harness.addToBattlefield(player1, new StompingGround());
        harness.addToBattlefield(player2, new StompingGround());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new CrashLanding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
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

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
}
