package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JuvenileMistDragon.class, GrizzlyBears.class})
class JuvenileMistDragonTest extends BaseCardTest {

    @Test
    void tapsAndSkipsNextUntapForOneCreatureAnOpponentControls() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        castJuvenileMistDragon(List.of(bear.getId()));

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isZero();
    }

    @Test
    void canChooseNoTargets() {
        castJuvenileMistDragon(List.of());

        harness.assertOnBattlefield(player1, "Juvenile Mist Dragon");
    }

    @Test
    void canDeclineTargetEvenWhenAnOpponentControlsACreature() {
        Permanent target = addCreatureReady(player2, new JuvenileMistDragon());

        castJuvenileMistDragon(List.of());

        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotChooseTwoCreaturesControlledByTheSameOpponent() {
        Permanent firstBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alreadyTappedTargetSkipsOnlyItsNextUntapStep() {
        Permanent target = addCreatureReady(player2, new JuvenileMistDragon());
        target.tap();

        castJuvenileMistDragon(List.of(target.getId()));

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void overlappingTriggersDoNotSkipTwoUntapSteps() {
        Permanent target = addCreatureReady(player2, new JuvenileMistDragon());

        castJuvenileMistDragon(List.of(target.getId()));
        castJuvenileMistDragon(List.of(target.getId()));

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetThatBecomesControlledByYouBeforeResolutionIsUnaffected() {
        Permanent target = addCreatureReady(player2, new JuvenileMistDragon());
        prepareCast();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    private void castJuvenileMistDragon(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new JuvenileMistDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
