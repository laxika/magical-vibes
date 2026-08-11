package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamThrushTest extends BaseCardTest {

    @Test
    void chosenTypeReplacesLandTypesUntilEndOfTurn() {
        Permanent forest = addDreamThrushAndForest(player1);

        activateAndChooseIsland(player1, forest);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    void canTargetAnOpponentsLand() {
        addCreatureReady(player1, new DreamThrush());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forest.getId());
    }

    @Test
    void cannotTargetANonLandPermanent() {
        addCreatureReady(player1, new DreamThrush());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
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
