package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonflySuit.class, BearerOfMemory.class})
class DragonflySuitTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent suit = addSuitReady(player1);

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    @Test
    void crewAnimatesSuitAndTapsCrew() {
        Permanent suit = addSuitReady(player1);
        Permanent crew = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isTrue();
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(2);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addSuitReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent suit = addSuitReady(player1);
        addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, suit)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    @Test
    void crewTapsAsACostButAnimatesOnlyWhenAbilityResolves() {
        Permanent suit = addSuitReady(player1);
        Permanent crew = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(suit.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, suit)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isTrue();
        assertThat(suit.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreatureCanCrewNewSuit() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new DragonflySuit());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        suit.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, suit)).isTrue();
        assertThat(suit.isSummoningSick()).isTrue();
    }

    @Test
    void tappedCreatureCannotCrew() {
        addSuitReady(player1);
        Permanent crew = addCreatureReady(player1, new BearerOfMemory());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void opposingCreatureCannotCrew() {
        addSuitReady(player1);
        Permanent opposingCreature = addCreatureReady(player2, new BearerOfMemory());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    void tappedSuitCanBeCrewed() {
        Permanent suit = addSuitReady(player1);
        suit.tap();
        Permanent crew = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isTrue();
        assertThat(suit.isTapped()).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void animatedSuitCannotCrewItself() {
        Permanent suit = addSuitReady(player1);
        addCreatureReady(player1, new BearerOfMemory());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(suit.isTapped()).isFalse();
    }

    private Permanent addSuitReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DragonflySuit());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
