package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorElixir;
import com.github.laxika.magicalvibes.cards.t.TraxosScourgeOfKroog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewArgive.class, TraxosScourgeOfKroog.class, GrizzlyBears.class,
        IchorElixir.class, Forest.class})
class NewArgiveTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new NewArgive(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void historicCreaturesYouControlGetPlusTwoPlusTwoWhenTheyAttack() {
        Permanent historic = addCreatureReady(player1, new TraxosScourgeOfKroog());
        Permanent nonhistoric = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHistoric = addCreatureReady(player2, new TraxosScourgeOfKroog());
        int historicPower = gqs.getEffectivePower(gd, historic);
        int historicToughness = gqs.getEffectiveToughness(gd, historic);
        int nonhistoricPower = gqs.getEffectivePower(gd, nonhistoric);
        int nonhistoricToughness = gqs.getEffectiveToughness(gd, nonhistoric);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, historic)).isEqualTo(historicPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, historic)).isEqualTo(historicToughness + 2);
        assertThat(gqs.getEffectivePower(gd, nonhistoric)).isEqualTo(nonhistoricPower);
        assertThat(gqs.getEffectiveToughness(gd, nonhistoric)).isEqualTo(nonhistoricToughness);
        assertThat(gqs.getEffectivePower(gd, opponentHistoric)).isEqualTo(historicPower);
    }

    @Test
    void chaosPutsTheFirstHistoricCardIntoYourHandAndTheRestOnTheBottom() {
        Card bottomCard = new Forest();
        Card beforeHistoric = new GrizzlyBears();
        Card historic = new IchorElixir();
        harness.setLibrary(player1, List.of(bottomCard, beforeHistoric, historic));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        harness.assertInHand(player1, "Ichor Elixir");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(bottomCard, beforeHistoric);
    }
}
