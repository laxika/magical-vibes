package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class SizeGatedRemovalPumpTest {

    private GameTestHarness harness;
    private Player ai;
    private Player human;
    private SizeGatedRemovalPump pump;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        ai = harness.getPlayer1();
        human = harness.getPlayer2();
        harness.skipMulligan();
        var amounts = new AmountEvaluationService(
                new PredicateEvaluationService(harness.getGameQueryService()),
                harness.getGameQueryService());
        pump = new SizeGatedRemovalPump(harness.getGameQueryService(), amounts);
    }

    @Test
    @DisplayName("Fit of Rage + Smite enables pumping an opponent 2/2")
    void findsOpponentCreatureEnabledByFitOfRageWithSmiteInHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        harness.setHand(ai, List.of(new FitOfRage(), new SmiteTheMonstrous()));

        var enabled = pump.findEnabledOpponentCreatures(
                harness.getGameData(),
                harness.getGameData().playerHands.get(ai.getId()).getFirst(),
                ai.getId(), human.getId());

        assertThat(enabled).extracting(Permanent::getId).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Fit of Rage alone does not treat opponent creatures as pump-to-kill setups")
    void noEnablementWithoutSizeGatedRemoval() {
        harness.addToBattlefield(human, new GrizzlyBears());
        harness.setHand(ai, List.of(new FitOfRage()));

        var enabled = pump.findEnabledOpponentCreatures(
                harness.getGameData(),
                harness.getGameData().playerHands.get(ai.getId()).getFirst(),
                ai.getId(), human.getId());

        assertThat(enabled).isEmpty();
    }
}