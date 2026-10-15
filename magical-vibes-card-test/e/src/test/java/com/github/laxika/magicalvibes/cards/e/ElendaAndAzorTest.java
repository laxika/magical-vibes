package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElendaAndAzor.class})
class ElendaAndAzorTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger pays its colored cost and draws X cards")
    void attackTriggerPaysColoredCostAndDraws() {
        addCreatureReady(player1, new ElendaAndAzor());
        harness.setLibrary(player1, List.of(new ElendaAndAzor(), new ElendaAndAzor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each end step may pay life to create tokens for cards drawn this turn")
    void endStepCreatesTokensForCardsDrawnThisTurn() {
        addCreatureReady(player1, new ElendaAndAzor());
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        harness.setLife(player1, 20);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Vampire Knight")).hasSize(2);
    }

    @Test
    @DisplayName("Declining the end-step payment creates no tokens")
    void decliningEndStepPaymentDoesNothing() {
        addCreatureReady(player1, new ElendaAndAzor());
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Vampire Knight")).isEmpty();
    }

    @Test
    @DisplayName("The attack payment remains optional when mana is available")
    void decliningAttackPaymentDoesNotSpendManaOrDraw() {
        addCreatureReady(player1, new ElendaAndAzor());
        harness.setLibrary(player1, List.of(new ElendaAndAzor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleXValueChosen(player1, 0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Paying the colored attack cost with X zero must remain available")
    void attackOffersPaymentWhenOnlyZeroIsAffordable() {
        addCreatureReady(player1, new ElendaAndAzor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Generic mana cannot replace the attack payment's colored requirements")
    void attackCannotPayWithoutColoredMana() {
        addCreatureReady(player1, new ElendaAndAzor());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("End-step tokens have the specified creature characteristics")
    void endStepCreatesBlackVampireKnightsWithLifelink() {
        addCreatureReady(player1, new ElendaAndAzor());
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Vampire Knight")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.KNIGHT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("End-step token count includes cards drawn after the ability triggers")
    void endStepCountsDrawsAtResolutionAndOnlyForController() {
        addCreatureReady(player1, new ElendaAndAzor());
        harness.setLibrary(player1, List.of(new ElendaAndAzor(), new ElendaAndAzor()));
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        gd.cardsDrawnThisTurn.put(player2.getId(), 5);
        harness.setLife(player1, 20);

        advanceToEndStep(player2);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Vampire Knight")).hasSize(2);
        assertThat(findPermanents(player2, "Vampire Knight")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Life may be paid at the end step even with no cards drawn")
    void endStepCanPayLifeForZeroTokens() {
        addCreatureReady(player1, new ElendaAndAzor());
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Vampire Knight")).isEmpty();
    }

    @Test
    @DisplayName("Less than four life cannot pay for end-step tokens")
    void insufficientLifeCannotCreateTokens() {
        addCreatureReady(player1, new ElendaAndAzor());
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        harness.setLife(player1, 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(findPermanents(player1, "Vampire Knight")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
