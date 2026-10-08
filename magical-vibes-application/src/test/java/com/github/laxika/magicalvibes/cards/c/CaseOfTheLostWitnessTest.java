package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FblthpTheLost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheLostWitness.class, FblthpTheLost.class, Forest.class, GrizzlyBears.class, Opt.class})
class CaseOfTheLostWitnessTest extends BaseCardTest {

    @Test
    void entersConjuresFourFblthpsThenDraws() {
        harness.setHand(player1, List.of(new CaseOfTheLostWitness()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Fblthp, the Lost");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsOnly("Fblthp, the Lost")
                .hasSize(3);
    }

    @Test
    void solvesOnlyWithLegendaryHomunculus() {
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLostWitness());
        harness.addToBattlefield(player1, new GrizzlyBears());

        resolveEndStepTriggers();
        assertThat(witness.isSolved()).isFalse();

        harness.addToBattlefield(player1, new FblthpTheLost());
        resolveEndStepTriggers();
        assertThat(witness.isSolved()).isTrue();
    }

    @Test
    void solvedCaseAllowsCastingSpellsAndPlayingLandsFromTop() {
        harness.addToBattlefield(player1, new CaseOfTheLostWitness());
        harness.addToBattlefield(player1, new FblthpTheLost());
        resolveEndStepTriggers();

        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Opt(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Forest");
    }

    @Test
    void unsolvedCaseCannotUseTopOfLibraryPermissions() {
        harness.addToBattlefield(player1, new CaseOfTheLostWitness());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
