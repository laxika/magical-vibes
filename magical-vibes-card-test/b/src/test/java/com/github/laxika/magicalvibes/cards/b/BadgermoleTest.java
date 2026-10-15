package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Badgermole.class, Forest.class, GrizzlyBears.class, TurnToFrog.class})
class BadgermoleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void earthbendsLandOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermole(land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredControlledCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new Badgermole());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot earthbend a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new Badgermole(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId())
                .doesNotContain(opponentLand.getId());

        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();
    }

    private void castBadgermole(java.util.UUID targetId) {
        harness.castFromHand(player1, new Badgermole(), "{4}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    @Test
    void earthbendedLandReturnsTappedAfterDeath() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermole(land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();
        assertReturnedLand(land);
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermole(land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.passBothPriorities();
        assertReturnedLand(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void earthbendReturnSurvivesLandLosingAbilities() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermole(land.getId());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();
        assertReturnedLand(land);
    }

    @Test
    void trampleTracksCountersAndIncludesSourceButNotOpponents() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        source.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();
    }

    private void assertReturnedLand(Permanent original) {
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(original.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }
}
