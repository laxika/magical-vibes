package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TophGreatestEarthbender.class, Forest.class, GrizzlyBears.class})
class TophGreatestEarthbenderTest extends BaseCardTest {

    @Test
    void earthbendsForTheManaSpentToCastHerAndGrantsDoubleStrikeToLandCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TophGreatestEarthbender(), "{2}{R}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId(), otherLand.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherLand, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void cannotEarthbendAnOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new TophGreatestEarthbender(), "{2}{R}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId()).doesNotContain(opponentLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLand, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void enteringWithoutBeingCastEarthbendsZeroAndReturnsTheLandTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new TophGreatestEarthbender());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void earthbendRemembersManaSpentWhenTophLeavesBeforeTheTriggerResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        TophGreatestEarthbender toph = new TophGreatestEarthbender();
        harness.castFromHand(player1, toph, "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(toph.getId()))
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void earthbendedLandReturnsTappedWithoutAnimationOrCounters(boolean exile) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new TophGreatestEarthbender(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            if (exile) {
                harness.getPermanentRemovalService().removePermanentToExile(gd, land);
            } else {
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land);
            }
        });
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void canCastTophWithoutControllingAnyLands() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new TophGreatestEarthbender(), "{2}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toph, Greatest Earthbender");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
    }
}