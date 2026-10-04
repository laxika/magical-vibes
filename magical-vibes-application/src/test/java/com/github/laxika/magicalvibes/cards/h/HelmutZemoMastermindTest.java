package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({HelmutZemoMastermind.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class HelmutZemoMastermindTest extends BaseCardTest {

    @Test
    @DisplayName("Targets only instants and sorceries within its power")
    void targetsCardsWithinPower() {
        Permanent helmut = addReadyHelmut();
        Card shock = new Shock();
        Card counsel = new CounselOfTheSoratami();
        Card creature = new GrizzlyBears();
        helmut.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(shock, counsel, creature));
        harness.setGraveyard(player2, List.of(new Shock()));

        declareAttack();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId(), counsel.getId());
    }

    @Test
    @DisplayName("Casts the chosen card with mana payment, exiles it, and puts a counter on Helmut")
    void castsChosenCardAndPutsCounterOnHelmut() {
        Permanent helmut = addReadyHelmut();
        helmut.setPowerModifier(2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setGraveyard(player1, List.of(counsel));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(helmut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Uses the attacker's power when checking the graveyard target")
    void targetMustStillFitPowerOnResolution() {
        Permanent helmut = addReadyHelmut();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        helmut.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(counsel));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        helmut.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Cannot cast the chosen spell without enough mana")
    void cannotCastWithoutMana() {
        Permanent helmut = addReadyHelmut();
        helmut.setPowerModifier(1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
        assertThat(gd.stack).isEmpty();
        assertThat(helmut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the cast leaves the card in the graveyard and adds no counter")
    void decliningCastAddsNoCounter() {
        Permanent helmut = addReadyHelmut();
        helmut.setPowerModifier(1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
        assertThat(gd.stack).isEmpty();
        assertThat(helmut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is placed during the attack ability's resolution")
    void counterIsPlacedBeforePlayersReceivePriority() {
        Permanent helmut = addReadyHelmut();
        helmut.setPowerModifier(1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(helmut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(counsel);
    }

    private Permanent addReadyHelmut() {
        Permanent helmut = harness.addToBattlefieldAndReturn(player1, new HelmutZemoMastermind());
        helmut.setSummoningSick(false);
        return helmut;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
