package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReefShaman.class, Forest.class, GrizzlyBears.class})
class ReefShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes the chosen basic land type until end of turn")
    void targetLandBecomesChosenType() {
        Permanent forest = addReefShamanAndForest();

        activateAndChooseIsland(forest);

        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The land type override wears off at end of turn")
    void overrideWearsOffAtEndOfTurn() {
        Permanent forest = addReefShamanAndForest();

        activateAndChooseIsland(forest);
        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("The ability can target an opponent's land")
    void canTargetOpponentsLand() {
        addCreatureReady(player1, new ReefShaman());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new ReefShaman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addReefShamanAndForest() {
        addCreatureReady(player1, new ReefShaman());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        return forest;
    }

    private void activateAndChooseIsland(Permanent forest) {
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
    }
}
