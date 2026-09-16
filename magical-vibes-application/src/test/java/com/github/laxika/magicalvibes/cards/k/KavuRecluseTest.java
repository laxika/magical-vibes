package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KavuRecluse.class, TerminalMoraine.class})
class KavuRecluseTest extends BaseCardTest {

    @Test
    void targetLandBecomesForestAndReplacesItsLandTypes() {
        Permanent land = addKavuAndTerminalMoraine();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, land);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void forestTypeWearsOffAtEndOfTurn() {
        Permanent land = addKavuAndTerminalMoraine();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
    }

    @Test
    void activatingAbilityTapsKavuRecluse() {
        Permanent land = addKavuAndTerminalMoraine();
        Permanent kavu = findPermanent(player1, "Kavu Recluse");

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(kavu.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    void canTargetAnOpponentsLand() {
        addCreatureReady(player1, new KavuRecluse());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void cannotTargetANonLandPermanent() {
        addCreatureReady(player1, new KavuRecluse());
        Permanent otherKavu = addCreatureReady(player1, new KavuRecluse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherKavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addKavuAndTerminalMoraine() {
        addCreatureReady(player1, new KavuRecluse());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        harness.forceActivePlayer(player1);
        return land;
    }
}
