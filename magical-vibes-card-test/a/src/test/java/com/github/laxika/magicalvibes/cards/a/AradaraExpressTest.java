package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.n.NarnamCobra;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AradaraExpress.class, BastionMastodon.class, NarnamCobra.class})
class AradaraExpressTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent express = addAradaraExpressReady(player1);

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    void crewWithEnoughPowerAnimatesExpressAndTapsCrew() {
        Permanent express = addAradaraExpressReady(player1);
        Permanent crew = addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(express.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addAradaraExpressReady(player1);
        addCreatureReady(player1, new NarnamCobra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent express = addAradaraExpressReady(player1);
        addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, express)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(express.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    private Permanent addAradaraExpressReady(Player player) {
        return addCreatureReady(player, new AradaraExpress());
    }

    @Test
    void twoCreaturesCanCombineTheirPowerToCrew() {
        Permanent express = addAradaraExpressReady(player1);
        Permanent first = addCreatureReady(player1, new NarnamCobra());
        Permanent second = addCreatureReady(player1, new NarnamCobra());

        harness.activateAbility(player1, 0, null, null);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(express.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreatureCanPayCrewCost() {
        Permanent express = addAradaraExpressReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isTrue();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        addAradaraExpressReady(player1);
        Permanent crew = addCreatureReady(player1, new BastionMastodon());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void opponentCreatureCannotPayCrewCost() {
        addAradaraExpressReady(player1);
        addCreatureReady(player2, new BastionMastodon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void menaceRejectsOneBlockerAfterCrewing() {
        addAradaraExpressReady(player1);
        addCreatureReady(player1, new BastionMastodon());
        addCreatureReady(player2, new NarnamCobra());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockersAfterCrewing() {
        addAradaraExpressReady(player1);
        addCreatureReady(player1, new BastionMastodon());
        Permanent first = addCreatureReady(player2, new NarnamCobra());
        Permanent second = addCreatureReady(player2, new NarnamCobra());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void canChooseMoreCrewAfterReachingRequiredPower() {
        Permanent express = addAradaraExpressReady(player1);
        Permanent first = addCreatureReady(player1, new BastionMastodon());
        Permanent second = addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isTrue();
    }
}
