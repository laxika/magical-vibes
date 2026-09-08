package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvasionSubmersible.class, GrizzlyBears.class, Island.class})
class InvasionSubmersibleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning up to one other nonland permanent to its owner's hand")
    void entersAndReturnsAnotherNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(target.getOriginalCard().getId()));
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Can enter without choosing an optional target")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Invasion Submersible");
    }

    @Test
    @DisplayName("The enter-the-battlefield target must be another nonland permanent")
    void rejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Waterbend permanently animates the Vehicle and adds three +1/+1 counters")
    void waterbendAnimatesAndAddsCounters() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(submersible.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.isArtifact(submersible)).isTrue();
        assertThat(gqs.isCreature(gd, submersible)).isTrue();
        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(3);
    }

    @Test
    @DisplayName("Waterbend cannot be activated more than once")
    void waterbendCanBeActivatedOnlyOnce() {
        harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
