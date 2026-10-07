package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BendersWaterskin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TophTheBlindBandit.class, Forest.class, GrizzlyBears.class,
        TophTheFirstMetalbender.class, BendersWaterskin.class})
class TophTheBlindBanditTest extends BaseCardTest {

    @Test
    void powerEqualsPlusOneCountersOnLandsYouControl() {
        Permanent toph = addCreatureReady(player1, new TophTheBlindBandit());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        firstLand.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        secondLand.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        opposingLand.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        assertThat(gqs.getEffectivePower(gd, toph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, toph)).isEqualTo(3);
    }

    @Test
    void entersAndEarthbendsTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TophTheBlindBandit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void earthbendRequiresLandYouControl() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TophTheBlindBandit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({TophTheBlindBandit.class, TophTheFirstMetalbender.class, BendersWaterskin.class})
    void countsCountersOnArtifactsThatHaveBecomeLands() {
        Permanent toph = harness.addToBattlefieldAndReturn(player1, new TophTheBlindBandit());
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.isLand(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, toph)).isEqualTo(4);
    }

    @Test
    void powerUpdatesWhenLandCountersChange() {
        Permanent toph = harness.addToBattlefieldAndReturn(player1, new TophTheBlindBandit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, toph)).isZero();
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        assertThat(gqs.getEffectivePower(gd, toph)).isEqualTo(4);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, toph)).isEqualTo(1);
    }

    @Test
    void earthbentLandReturnsTappedWithoutAnimationAfterDying() {
        Permanent land = earthbendForest();
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertReturnedForest(land);
    }

    @Test
    void earthbentLandReturnsTappedWithoutAnimationAfterExile() {
        Permanent land = earthbendForest();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.passBothPriorities();

        assertReturnedForest(land);
    }

    private Permanent earthbendForest() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TophTheBlindBandit()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        return land;
    }

    private void assertReturnedForest(Permanent oldLand) {
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(oldLand.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Toph, the Blind Bandit"))).isZero();
    }
}
