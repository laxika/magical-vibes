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
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(com.github.laxika.magicalvibes.model.CardSubtype.KNIGHT);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    private void addAllianceAndCreatures(int creatureCount) {
        harness.addToBattlefieldAndReturn(player1, new ChivalricAlliance());
        for (int i = 0; i < creatureCount; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }
}
