package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadjanSpirit.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class RadjanSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses flying until end of turn")
    void targetLosesFlyingUntilEndOfTurn() {
        Permanent spirit = addCreatureReady(player1, new RadjanSpirit());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, null, elemental.getId());
        assertThat(spirit.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature without flying (no effect on it)")
    void targetsNonFlyer() {
        addCreatureReady(player1, new RadjanSpirit());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new RadjanSpirit());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, elemental.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, elemental));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(elemental.getCard());
        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new RadjanSpirit());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
