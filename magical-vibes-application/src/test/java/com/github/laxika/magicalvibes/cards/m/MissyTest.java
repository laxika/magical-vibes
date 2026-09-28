package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldForgedSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesMissyVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Missy.class, GrizzlyBears.class, GoldForgedSentinel.class})
class MissyTest extends BaseCardTest {

    @Test
    void returnsAnotherNonartifactCreatureAsTappedFaceDownCybermanUnderYourControl() {
        harness.addToBattlefield(player1, new Missy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getFaceDownPower()).isEqualTo(2);
        assertThat(returned.getFaceDownToughness()).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, returned))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.CYBERMAN)).isTrue();
    }

    @Test
    void artifactCreatureDeathDoesNotReturn() {
        harness.addToBattlefield(player1, new Missy());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel());
        artifact.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(artifact.getCard().getId()));
    }

    @Test
    void opponentChoosesArtifactDamageOrControllerDraws() {
        harness.addToBattlefield(player1, new Missy());
        harness.addToBattlefield(player1, new GoldForgedSentinel());
        var libraryBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryBears));

        resolveEndStep();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION,
                EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION);

        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(libraryBears.getId()));
    }

    @Test
    void opponentCanChooseControllerDrawsAndChaos() {
        harness.addToBattlefield(player1, new Missy());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveEndStep();
        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
