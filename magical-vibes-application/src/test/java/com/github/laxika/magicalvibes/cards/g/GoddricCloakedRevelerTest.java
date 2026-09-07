package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoddricCloakedReveler.class, DragonEgg.class, Forest.class, GrizzlyBears.class})
class GoddricCloakedRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 after two nonland permanents enter under your control this turn")
    void becomesDragonAfterCelebration() {
        Permanent goddric = castGoddric();

        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, goddric)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goddric)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent goddric = castGoddric();
        int powerBeforeLand = gqs.getEffectivePower(gd, goddric);
        int toughnessBeforeLand = gqs.getEffectiveToughness(gd, goddric);
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        assertThat(gqs.getEffectivePower(gd, goddric)).isEqualTo(powerBeforeLand);
        assertThat(gqs.getEffectiveToughness(gd, goddric)).isEqualTo(toughnessBeforeLand);
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent goddric = castGoddric();
        int powerBeforeCelebration = gqs.getEffectivePower(gd, goddric);
        int toughnessBeforeCelebration = gqs.getEffectiveToughness(gd, goddric);
        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, goddric)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goddric)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goddric)).isEqualTo(powerBeforeCelebration);
        assertThat(gqs.getEffectiveToughness(gd, goddric)).isEqualTo(toughnessBeforeCelebration);
    }

    @Test
    @DisplayName("Granted ability boosts Dragons you control only")
    void grantedAbilityBoostsDragonsYouControl() {
        Permanent goddric = castGoddric();
        castGrizzlyBears();
        goddric.setSummoningSick(false);

        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonEgg());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new DragonEgg());
        int goddricPower = gqs.getEffectivePower(gd, goddric);
        int dragonPower = gqs.getEffectivePower(gd, dragon);
        int ownBearsPower = gqs.getEffectivePower(gd, ownBears);
        int opponentDragonPower = gqs.getEffectivePower(gd, opponentDragon);

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(goddric);
        harness.activateAbility(player1, sourceIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goddric)).isEqualTo(goddricPower + 1);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(dragonPower + 1);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(ownBearsPower);
        assertThat(gqs.getEffectivePower(gd, opponentDragon)).isEqualTo(opponentDragonPower);
    }

    private Permanent castGoddric() {
        harness.setHand(player1, List.of(new GoddricCloakedReveler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Goddric, Cloaked Reveler");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
