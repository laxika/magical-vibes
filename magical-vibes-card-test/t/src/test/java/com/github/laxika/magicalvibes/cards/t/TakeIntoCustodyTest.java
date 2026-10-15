package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.h.HeroicIntervention;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeIntoCustody.class, GrizzlyBears.class, Forest.class, DruidOfTheCowl.class,
        HeroicIntervention.class})
class TakeIntoCustodyTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and skips its controller's next untap step")
    void tapsCreatureAndSkipsNextUntapStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTakeIntoCustody(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TakeIntoCustody()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped creature still misses its next untap")
    void alreadyTappedCreatureSkipsNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        creature.tap();
        castTakeIntoCustody(creature);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target your own creature without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        other.tap();
        castTakeIntoCustody(target);

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature gaining hexproof in response is neither tapped nor restricted")
    void hexproofInResponseMakesTargetIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.setHand(player1, List.of(new TakeIntoCustody()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new HeroicIntervention()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two casts before the same untap step do not skip two steps")
    void repeatedCastsSkipOnlyOneUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        castTakeIntoCustody(creature);
        castTakeIntoCustody(creature);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    private void castTakeIntoCustody(Permanent target) {
        harness.setHand(player1, List.of(new TakeIntoCustody()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
