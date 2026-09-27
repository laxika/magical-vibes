package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FlowOfIdeas;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfTheStorm.class, FlowOfIdeas.class, Island.class, TellingTime.class})
class EyeOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a cast instant or sorcery and offers its copy")
    void exilesCastSpellAndOffersCopy() {
        UUID eyeId = addEye();
        harness.addToBattlefield(player1, new Island());
        TellingTime drawnCard = new TellingTime();
        harness.setLibrary(player1, List.of(drawnCard));
        FlowOfIdeas flowOfIdeas = new FlowOfIdeas();

        harness.castFromHand(player1, flowOfIdeas, "{5}{U}");
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getId))
                .containsExactly(flowOfIdeas.getId());
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCopyIds()).hasSize(1);

        harness.handleMultipleCardsChosen(player1, choice.validCopyIds());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Copies all tracked spells in the order chosen by the caster")
    void copiesAllTrackedSpellsInChosenOrder() {
        UUID eyeId = addEye();
        FlowOfIdeas flowOfIdeas = new FlowOfIdeas();

        harness.castFromHand(player1, flowOfIdeas, "{5}{U}");
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice firstChoice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, firstChoice.validCopyIds());
        harness.passBothPriorities();

        TellingTime tellingTime = new TellingTime();
        harness.castFromHand(player1, tellingTime, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getName))
                .containsExactly("Flow of Ideas", "Telling Time");
        PendingInteraction.EyeOfTheStormCastChoice secondChoice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        List<UUID> chosenInReverseOrder = List.of(
                secondChoice.validCopyIds().get(1), secondChoice.validCopyIds().getFirst());

        harness.handleMultipleCardsChosen(player1, chosenInReverseOrder);

        assertThat(gd.stack.stream().filter(StackEntry::isCopy).map(entry -> entry.getCard().getName()))
                .containsExactly("Telling Time", "Flow of Ideas");
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType().name().equals("TRIGGERED_ABILITY")
                && entry.getCard().getName().equals("Eye of the Storm"));
    }

    @Test
    @DisplayName("Lets the caster decline every generated copy")
    void mayDeclineEveryCopy() {
        UUID eyeId = addEye();
        FlowOfIdeas flowOfIdeas = new FlowOfIdeas();

        harness.castFromHand(player1, flowOfIdeas, "{5}{U}");
        harness.passBothPriorities();

        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        UUID copyId = choice.validCopyIds().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getId))
                .containsExactly(flowOfIdeas.getId());
    }

    @Test
    @DisplayName("Triggers for an instant cast by another player")
    void triggersForAnotherPlayersSpell() {
        UUID eyeId = addEye();
        TellingTime tellingTime = new TellingTime();
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, tellingTime, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getId))
                .containsExactly(tellingTime.getId());
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, choice.validCopyIds());

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Telling Time"));
    }

    private UUID addEye() {
        harness.addToBattlefield(player1, new EyeOfTheStorm());
        return harness.getPermanentId(player1, "Eye of the Storm");
    }
}
