package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EffieFastLearner.class, GrizzlyBears.class})
class EffieFastLearnerTest extends BaseCardTest {

    @Test
    void survivalCountersTappedCreaturesAndSeeksEligibleSurvivor() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        effie.tap();
        tappedCreature.tap();

        Card eligibleSurvivor = survivorCard("{2}");
        Card expensiveSurvivor = survivorCard("{3}");
        Card nonSurvivor = new GrizzlyBears();
        nonSurvivor.setManaCost("{1}");
        harness.setLibrary(player1, List.of(eligibleSurvivor, expensiveSurvivor, nonSurvivor));

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, effie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, effie)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleSurvivor);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveSurvivor, nonSurvivor);
    }

    @Test
    void untappedEffieDoesNotTriggerSurvival() {
        harness.addToBattlefield(player1, new EffieFastLearner());
        harness.setLibrary(player1, List.of(survivorCard("{0}")));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enlistTapsAProperSupporterAndAddsItsPower() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, effie)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, effie)).isEqualTo(3);
    }

    private Card survivorCard(String manaCost) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.SURVIVOR));
        card.setManaCost(manaCost);
        return card;
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
