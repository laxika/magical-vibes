package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeyamShaiMurad.class, Forest.class, GrizzlyBears.class, Shock.class})
class NeyamShaiMuradTest extends BaseCardTest {

    @Test
    void canDeclineReturnWhenTargetedAbilityResolves() {
        Card opponentForest = new Forest();
        harness.setGraveyard(player2, List.of(opponentForest));
        harness.setGraveyard(player1, List.of(new Forest()));

        Permanent neyam = addCreatureReady(player1, new NeyamShaiMurad());
        neyam.setAttacking(true);
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(opponentForest.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalGraveyardTargetPreventsBothReturns() {
        Card opponentForest = new Forest();
        harness.setGraveyard(player2, List.of(opponentForest));
        harness.setGraveyard(player1, List.of(new Forest()));

        Permanent neyam = addCreatureReady(player1, new NeyamShaiMurad());
        neyam.setAttacking(true);
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(opponentForest.getId()));
        harness.setGraveyard(player2, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }


    @Test
    @DisplayName("Returns a targeted opponent permanent, then returns the defending player's choice")
    void returnsTargetedPermanentThenDefendingPlayerChoosesOwnPermanent() {
        Card opponentForest = new Forest();
        Card ownBears = new GrizzlyBears();
        Card ownForest = new Forest();
        harness.setGraveyard(player2, List.of(opponentForest));
        harness.setGraveyard(player1, List.of(ownBears, ownForest));

        Permanent neyam = addCreatureReady(player1, new NeyamShaiMurad());
        neyam.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(opponentForest.getId()));
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int chosenIndex = choice.cardPool().stream().map(Card::getId).toList().indexOf(ownBears.getId());
        assertThat(chosenIndex).isGreaterThanOrEqualTo(0);
        harness.handleGraveyardCardChosen(player2, chosenIndex);
        resolveAllTriggers();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getGrantedSubtypes()).doesNotContain(com.github.laxika.magicalvibes.model.CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Ignores nonpermanent cards in the damaged player's graveyard")
    void ignoresNonpermanentOpponentGraveyardCards() {
        harness.setGraveyard(player2, List.of(new Shock()));

        Permanent neyam = addCreatureReady(player1, new NeyamShaiMurad());
        neyam.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }
}
