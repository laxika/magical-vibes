package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ChivalricAlliance.class, GrizzlyBears.class})
class ChivalricAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two or more creatures draws a card")
    void attackingWithTwoCreaturesDrawsCard() {
        addAllianceAndCreatures(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking with fewer than two creatures does not draw a card")
    void attackingWithOneCreatureDoesNotDrawCard() {
        addAllianceAndCreatures(1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Paying mana and discarding a card creates a vigilant Azorius Knight")
    void activatedAbilityCreatesKnightToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new ChivalricAlliance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent token = findPermanent(player1, "Knight");
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(com.github.laxika.magicalvibes.model.CardSubtype.KNIGHT);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with three creatures draws only one card")
    void attackingWithThreeCreaturesDrawsOnlyOneCard() {
        addAllianceAndCreatures(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ChivalricAlliance(), new ChivalricAlliance()));

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent attacking with two creatures does not trigger Alliance")
    void opposingAttackDoesNotDrawCard() {
        harness.addToBattlefield(player1, new ChivalricAlliance());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ChivalricAlliance()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0, 1));
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature card can be discarded and the ability works on an opponent's turn")
    void canDiscardEnchantmentOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ChivalricAlliance());
        harness.setHand(player1, List.of(new ChivalricAlliance()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Chivalric Alliance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Knight")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(countPermanents(player2, "Knight")).isZero();
    }

    @Test
    @DisplayName("Activation requires a card to discard")
    void cannotActivateWithEmptyHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ChivalricAlliance());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires two mana")
    void cannotActivateWithOnlyOneMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ChivalricAlliance());
        harness.setHand(player1, List.of(new ChivalricAlliance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addAllianceAndCreatures(int creatureCount) {
        harness.addToBattlefieldAndReturn(player1, new ChivalricAlliance());
        for (int i = 0; i < creatureCount; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }
}
