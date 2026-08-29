package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmergencyPowersTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard into their library and draws seven cards")
    void shufflesHandsAndGraveyardsAndDrawsSeven() {
        forceMainPhase();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        harness.setHand(player1, List.of(emergencyPowers));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLibrary(player1, shocks(6));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(emergencyPowers.getId());
    }

    @Test
    @DisplayName("During your main phase, addendum may put one eligible permanent from hand onto the battlefield")
    void addendumPutsEligiblePermanentOntoBattlefield() {
        forceMainPhase();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(emergencyPowers, bears, shock));
        harness.setLibrary(player1, forests(5));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).contains(bears.getId()).doesNotContain(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Addendum does not apply when Emergency Powers is cast outside a main phase")
    void addendumDoesNotApplyOutsideMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(emergencyPowers, bears));
        harness.setLibrary(player1, forests(6));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addEmergencyPowersMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private List<Card> forests(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Forest())
                .toList();
    }

    private List<Card> shocks(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Shock())
                .toList();
    }
}
