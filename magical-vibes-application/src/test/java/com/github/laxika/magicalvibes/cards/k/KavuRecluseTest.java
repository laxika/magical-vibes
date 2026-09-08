package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KavuRecluseTest extends BaseCardTest {

    @Test
    void targetLandBecomesForestAndReplacesItsLandTypes() {
        Permanent land = addKavuAndIsland();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, land);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void forestTypeWearsOffAtEndOfTurn() {
        Permanent land = addKavuAndIsland();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    void cannotTargetANonLandPermanent() {
        addCreatureReady(player1, new KavuRecluse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addKavuAndIsland() {
        addCreatureReady(player1, new KavuRecluse());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.forceActivePlayer(player1);
        return land;
    }
}
