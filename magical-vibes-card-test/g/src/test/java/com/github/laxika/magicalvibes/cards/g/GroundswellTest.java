package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Groundswell.class, Forest.class, HillGiant.class})
class GroundswellTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/+2 without landfall")
    void givesPlusTwoPlusTwoWithoutLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGroundswell(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Gives target creature +4/+4 after landfall")
    void givesPlusFourPlusFourWithLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Forest(), new Groundswell()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Groundswell()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsLandEntryDoesNotEnableLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        castGroundswell(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void nonlandEntryDoesNotEnableLandfall() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new HillGiant());

        castGroundswell(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void landfallIsCheckedAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Groundswell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void landEnteringAfterResolutionDoesNotIncreaseBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castGroundswell(creature);

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void landfallStillAppliesAfterEnteredLandLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent land = harness.enterBattlefieldAndReturn(player1, new Forest());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());

        castGroundswell(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void multipleLandEntriesGiveOnlyPlusFourPlusFour() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        castGroundswell(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void boostExpiresAndLandfallHistoryResetsOnNextTurn(boolean landfall) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        if (landfall) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
        }
        castGroundswell(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(landfall ? 7 : 5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(landfall ? 7 : 5);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        castGroundswell(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    private void castGroundswell(Permanent target) {
        harness.setHand(player1, List.of(new Groundswell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
