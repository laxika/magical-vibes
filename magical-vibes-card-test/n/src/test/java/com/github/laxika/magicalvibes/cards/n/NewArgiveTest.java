package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorElixir;
import com.github.laxika.magicalvibes.cards.r.RishkarPeemaRenegade;
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
        IchorElixir.class, Forest.class, BurnishedHart.class, RishkarPeemaRenegade.class,
        NightsWhisper.class})
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

    @Test
    void nonlegendaryArtifactsAndNonartifactLegendariesBothGetTheAttackBoost() {
        Permanent artifact = addCreatureReady(player1, new BurnishedHart());
        Permanent legendary = addCreatureReady(player1, new RishkarPeemaRenegade());
        Permanent nonattacker = addCreatureReady(player1, new BurnishedHart());
        int artifactPower = gqs.getEffectivePower(gd, artifact);
        int artifactToughness = gqs.getEffectiveToughness(gd, artifact);
        int legendaryPower = gqs.getEffectivePower(gd, legendary);
        int legendaryToughness = gqs.getEffectiveToughness(gd, legendary);
        int nonattackerPower = gqs.getEffectivePower(gd, nonattacker);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(artifactPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(artifactToughness + 2);
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(legendaryPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(legendaryToughness + 2);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(nonattackerPower);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(artifactPower);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(artifactToughness);
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(legendaryPower);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(legendaryToughness);
    }

    @Test
    void chaosLeavesUnrevealedCardsOnTopAndMovesOnlyRevealedNonhistoricCardsToTheBottom() {
        Card revealed = new NightsWhisper();
        Card historic = new RishkarPeemaRenegade();
        Card next = new IchorElixir();
        Card last = new BurnishedHart();
        harness.setLibrary(player1, List.of(revealed, historic, next, last));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(historic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, last, revealed);
    }

    @Test
    void chaosWithAHistoricCardOnTopDoesNotDisturbTheRemainingLibrary() {
        Card historic = new BurnishedHart();
        Card next = new NightsWhisper();
        Card last = new IchorElixir();
        harness.setLibrary(player1, List.of(historic, next, last));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(historic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, last);
    }

    @Test
    void chaosWithNoHistoricCardReturnsEveryRevealedCardToTheLibrary() {
        Card first = new NightsWhisper();
        Card second = new NightsWhisper();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void chaosWithAnEmptyLibraryDoesNotPutAnythingIntoHand() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
