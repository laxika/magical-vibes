package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimulacrumSynthesizer.class, Manalith.class, MindStone.class, HillGiant.class,
        GrizzlyBears.class, HangarbackWalker.class, Naturalize.class})
class SimulacrumSynthesizerTest extends BaseCardTest {

    @Test
    void entersAndScriesTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new SimulacrumSynthesizer(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void qualifyingArtifactCreatesConstructThatScalesWithArtifacts() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.addToBattlefield(player1, new MindStone());
        harness.castFromHand(player1, new Manalith(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct).isNotNull();
        assertThat(construct.getCard().isToken()).isTrue();
        assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);
    }

    @Test
    void artifactWithManaValueBelowThreeDoesNotCreateConstruct() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.castFromHand(player1, new MindStone(), "{2}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonartifactWithManaValueAtLeastThreeDoesNotCreateConstruct() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringSynthesizerDoesNotTriggerItself() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SimulacrumSynthesizer(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryCanBottomOneCardAndLeavesTheThirdCardUnexamined() {
        GrizzlyBears first = new GrizzlyBears();
        MindStone second = new MindStone();
        HillGiant third = new HillGiant();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new SimulacrumSynthesizer(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherSynthesizerTriggersTheExistingOneOnly() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SimulacrumSynthesizer(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).hasSize(1);
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsQualifyingArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());

        harness.enterBattlefieldAndReturn(player2, new Manalith());

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(findPermanents(player2, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachQualifyingArtifactCreatesAnotherConstructAndGrowsExistingTokens() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.castFromHand(player1, new Manalith(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Construct")).hasSize(1);

        harness.castFromHand(player1, new Manalith(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).hasSize(2).allSatisfy(construct -> {
            assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(5);
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void constructCountsOnlyItsControllersArtifactsAndShrinksWhenOneLeaves() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new Manalith());
        harness.castFromHand(player1, new Manalith(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
    }

    @Test
    void triggerSurvivesSourceRemovalAndConstructKeepsItsOwnAbility() {
        Permanent synthesizer = harness.addToBattlefieldAndReturn(player1, new SimulacrumSynthesizer());
        harness.castFromHand(player1, new Manalith(), "{3}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, synthesizer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Simulacrum Synthesizer");
        assertThat(findPermanents(player1, "Construct")).hasSize(1);
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Manalith").getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).containsExactly(construct);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
    }

    @Test
    void xInAnEnteringArtifactsManaCostIsZeroEvenAfterPayingFourMana() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hangarback Walker");
        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
