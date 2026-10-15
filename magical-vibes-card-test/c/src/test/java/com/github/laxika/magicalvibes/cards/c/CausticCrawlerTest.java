package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shock.class, CausticCrawler.class, Forest.class, GrizzlyBears.class, TectonicEdge.class, WalkingAtlas.class})
class CausticCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may give a target creature -1/-1 until end of turn")
    void landfallGivesTargetCreatureMinusOneMinusOne() {
        addCrawler();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Declining landfall leaves the target creature unchanged")
    void decliningLandfallDoesNothing() {
        addCrawler();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Caustic Crawler")
    void opponentLandDoesNotTrigger() {
        addCrawler();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Landfall cannot target a noncreature permanent")
    void landfallRejectsNoncreatureTarget() {
        addCrawler();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Landfall can target Caustic Crawler itself")
    void landfallCanTargetItself() {
        Permanent crawler = addCrawler();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, crawler.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(crawler.getEffectivePower()).isEqualTo(3);
        assertThat(crawler.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall puts a creature with zero toughness into its owner's graveyard")
    void landfallKillsOneToughnessCreature() {
        addCrawler();
        Permanent atlas = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, atlas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Walking Atlas");
        harness.assertInGraveyard(player2, "Walking Atlas");
    }

    @Test
    @DisplayName("Each Crawler triggers independently and their reductions accumulate")
    void multipleCrawlersGiveSeparateCumulativeReductions() {
        Permanent target = addCrawler();
        addCrawler();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().getFirst());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, true));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCrawler() {
        return harness.addToBattlefieldAndReturn(player1, new CausticCrawler());
    }
}
