package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DavrosDalekCreator.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class DavrosDalekCreatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Dalek and lets a qualifying opponent choose to draw")
    void createsDalekAndOpponentChoosesDraw() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var drawn = new GrizzlyBears();
        var opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(opponentCard));
        dealThreeDamageToOpponent();

        resolveEndStep();

        harness.assertOnBattlefield(player1, "Dalek");
        var dalek = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dalek"))
                .findFirst().orElseThrow();
        assertThat(dalek.getCard().isToken()).isTrue();
        assertThat(dalek.getCard().getPower()).isEqualTo(3);
        assertThat(dalek.getCard().getToughness()).isEqualTo(3);
        assertThat(dalek.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(dalek.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(dalek.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(dalek.getCard().getSubtypes()).contains(CardSubtype.DALEK);
        assertThat(dalek.getCard().getKeywords()).contains(Keyword.MENACE);
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                ChoiceContext.VillainousChoice.DRAW,
                ChoiceContext.VillainousChoice.DISCARD);

        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DRAW);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Lets a qualifying opponent choose to discard")
    void opponentChoosesDiscard() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        dealThreeDamageToOpponent();

        resolveEndStep();

        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player1, "Dalek");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Triggers but does nothing when the opponent lost less than 3 life")
    void resolvesWithoutEffectBelowLifeThreshold() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        beginEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Dalek");
    }

    @Test
    @DisplayName("An empty-handed opponent can choose to discard without granting a draw")
    void emptyHandStillAllowsDiscardChoice() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var undrawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player2, List.of());
        dealThreeDamageToOpponent();

        resolveEndStep();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).contains(ChoiceContext.VillainousChoice.DISCARD);
        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DISCARD);

        harness.assertOnBattlefield(player1, "Dalek");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(undrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(undrawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life lost in response to the end-step ability qualifies")
    void lifeLostAfterTriggerQualifies() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        beginEndStep();
        assertThat(gd.stack).hasSize(1);
        dealThreeDamageToOpponent();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dalek");
        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DRAW);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Multiple life-loss events reaching the threshold create only one Dalek")
    void cumulativeLifeLossCreatesOneDalek() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dalek"))).hasSize(1);
        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DRAW);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Life lost by Davros's controller does not qualify")
    void controllerLifeLossDoesNotQualify() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        resolveEndStep();

        harness.assertNotOnBattlefield(player1, "Dalek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void opponentEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        dealThreeDamageToOpponent();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Dalek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void dealThreeDamageToOpponent() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void resolveEndStep() {
        beginEndStep();
        harness.passBothPriorities();
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
