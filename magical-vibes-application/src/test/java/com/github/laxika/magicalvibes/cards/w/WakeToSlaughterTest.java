package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({WakeToSlaughter.class, GrizzlyBears.class, LlanowarElves.class})
class WakeToSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the card for hand and the other returns with haste")
    void opponentChoosesCardForHand() {
        Card handCard = new GrizzlyBears();
        Card battlefieldCard = new LlanowarElves();
        castWithTargets(handCard, battlefieldCard);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(handCard, battlefieldCard);

        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().equals(battlefieldCard))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(returned.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.equals(battlefieldCard));
    }

    @Test
    @DisplayName("With one target, it returns to hand without an opponent choice")
    void oneTargetReturnsToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void castWithTargets(Card first, Card second) {
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
