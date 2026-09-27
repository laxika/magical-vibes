package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamThrush.class, Forest.class})
class DreamThrushTest extends BaseCardTest {

    @Test
    void chosenTypeReplacesLandTypesUntilEndOfTurn() {
        Permanent forest = addDreamThrushAndForest(player1);

        activateAndChooseIsland(player1, forest);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void chosenTypeReplacesTheLandsManaAbility() {
        Permanent forest = addDreamThrushAndForest(player1);

        activateAndChooseIsland(player1, forest);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(0);
    }

    @Test
    void activationTapsDreamThrush() {
        Permanent dreamThrush = addCreatureReady(player1, new DreamThrush());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(dreamThrush.isTapped()).isTrue();
    }

    @Test
    void canTargetAnOpponentsLand() {
        addCreatureReady(player1, new DreamThrush());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forest.getId());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    void cannotTargetANonLandPermanent() {
        addCreatureReady(player1, new DreamThrush());
        harness.addToBattlefield(player1, new Forest());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new DreamThrush());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonland.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addDreamThrushAndForest(com.github.laxika.magicalvibes.model.Player player) {
        addCreatureReady(player, new DreamThrush());
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        harness.forceActivePlayer(player);
        return forest;
    }

    private void activateAndChooseIsland(com.github.laxika.magicalvibes.model.Player player, Permanent forest) {
        UUID forestId = forest.getId();
        harness.activateAbility(player, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player, "ISLAND");
    }
}
