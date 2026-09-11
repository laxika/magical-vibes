package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
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

@CardUsed({SelflessSamurai.class, MothriderSamurai.class, ElvishWarrior.class, GrizzlyBears.class, DoomBlade.class})
class SelflessSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai attacking alone gains lifelink until end of turn")
    void samuraiAttackingAloneGainsLifelink() {
        addCreatureReady(player1, new SelflessSamurai());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, samurai, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A Warrior attacking alone gains lifelink until end of turn")
    void warriorAttackingAloneGainsLifelink() {
        addCreatureReady(player1, new SelflessSamurai());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A non-Samurai, non-Warrior attacking alone does not gain lifelink")
    void otherCreatureAttackingAloneDoesNotGainLifelink() {
        addCreatureReady(player1, new SelflessSamurai());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SelflessSamurai());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, samurai, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing grants indestructible to another creature you control")
    void sacrificeGrantsIndestructible() {
        harness.addToBattlefield(player1, new SelflessSamurai());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Selfless Samurai");
    }

    @Test
    @DisplayName("Indestructible protects the target from destruction until end of turn")
    void targetSurvivesDestruction() {
        harness.addToBattlefield(player1, new SelflessSamurai());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target itself or an opposing creature")
    void restrictsActivationTarget() {
        Permanent samurai = harness.addToBattlefieldAndReturn(player1, new SelflessSamurai());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, samurai.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }
}
