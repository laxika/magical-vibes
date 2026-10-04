package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.cards.s.SunCollaredRaptor;
import com.github.laxika.magicalvibes.cards.s.SecretsOfTheGoldenCity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzorTheLawbringer.class, Bombard.class, SunCollaredRaptor.class, SecretsOfTheGoldenCity.class})
class AzorTheLawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability stops opponents from casting instants and sorceries next turn")
    void restrictsOpponentsDuringTheirNextTurn() {
        castAzor();

        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Azor, the Lawbringer").getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, findPermanent(player1, "Azor, the Lawbringer").getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new SunCollaredRaptor()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Sun-Collared Raptor");
    }

    @Test
    @DisplayName("Paying {X}{W}{U}{U} on attack gains X life and draws X cards")
    void payingOnAttackGainsLifeAndDrawsCards() {
        addCreatureReady(player1, new AzorTheLawbringer());
        addCreatureReady(player2, new SunCollaredRaptor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the optional attack payment does not gain life or draw cards")
    void choosingZeroDoesNothing() {
        addCreatureReady(player1, new AzorTheLawbringer());
        addCreatureReady(player2, new SunCollaredRaptor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The next-turn restriction includes sorceries and expires after that turn")
    void sorceryRestrictionExpiresAfterOpponentsNextTurn() {
        castAzor();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SecretsOfTheGoldenCity()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof SecretsOfTheGoldenCity);
    }

    @Test
    @DisplayName("The controller can pay for X=0 with exactly white and two blue mana")
    void offersPaymentWhenOnlyTheFixedCostIsAvailable() {
        addCreatureReady(player1, new AzorTheLawbringer());
        addCreatureReady(player2, new SunCollaredRaptor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isZero();
    }

    @Test
    @DisplayName("The attack payment cannot use generic mana in place of the required colors")
    void cannotPayWithoutRequiredColoredMana() {
        addCreatureReady(player1, new AzorTheLawbringer());
        addCreatureReady(player2, new SunCollaredRaptor());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void castAzor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AzorTheLawbringer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

}
