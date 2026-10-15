package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlassdustHulk.class, GlazeFiend.class})
class GlassdustHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact you control entering gives Glassdust Hulk +1/+1 and makes it unblockable")
    void allyArtifactEnterBoostsAndUnblockable() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities(); // resolve spell and collect the artifact-entry ability
        harness.passBothPriorities(); // resolve the artifact-entry ability

        assertThat(hulk.getPowerModifier()).isEqualTo(1);
        assertThat(hulk.getToughnessModifier()).isEqualTo(1);
        assertThat(hulk.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockable wear off at end of turn")
    void boostAndUnblockableWearOffAtCleanup() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(hulk.getPowerModifier()).isEqualTo(1);
        assertThat(hulk.isCantBeBlocked()).isTrue();

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(0);
        assertThat(hulk.getToughnessModifier()).isEqualTo(0);
        assertThat(hulk.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Glassdust Hulk")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(0);
        assertThat(hulk.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("One artifact entry creates one ability that resolves both effects together")
    void artifactEntryResolvesBothEffectsAsOneAbility() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.castFromHand(player1, new GlassdustHulk(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(hulk.getPowerModifier()).isEqualTo(1);
        assertThat(hulk.getToughnessModifier()).isEqualTo(1);
        assertThat(hulk.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Glassdust Hulk does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new GlassdustHulk(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent hulk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
        assertThat(hulk.isCantBeBlocked()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    @DisplayName("Cycling accepts either hybrid color, discards immediately, and draws on resolution")
    void cyclingWithEitherHybridColor(ManaColor color) {
        GlassdustHulk cycled = new GlassdustHulk();
        GlassdustHulk drawn = new GlassdustHulk();
        harness.setHand(player1, List.of(cycled));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, color, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
    }
}
