package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FlowOfIdeas;
import com.github.laxika.magicalvibes.cards.f.FieryConclusion;
import com.github.laxika.magicalvibes.cards.g.GreaterMossdog;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfTheStorm.class, FieryConclusion.class, FlowOfIdeas.class,
        GreaterMossdog.class, Island.class, TellingTime.class})
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
        return harness.addToBattlefieldAndReturn(player1, new EyeOfTheStorm()).getId();
    }

    @Test
    @DisplayName("A copy with a payable additional cost is offered the choices needed to cast it")
    void canPayAdditionalCostForCopy() {
        UUID eyeId = addEye();
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());
        FieryConclusion spell = new FieryConclusion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), firstSacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId)).containsExactly(spell);
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, choice.validCopyIds());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondSacrifice);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Multiple Eyes exile a spell only once and finish both triggers")
    void multipleEyesTrackSeparateCards() {
        UUID firstEyeId = addEye();
        UUID secondEyeId = addEye();
        FlowOfIdeas spell = new FlowOfIdeas();

        harness.castFromHand(player1, spell, "{5}{U}");
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(List.of(gd.getCardsExiledByPermanent(firstEyeId).size(),
                gd.getCardsExiledByPermanent(secondEyeId).size())).containsExactlyInAnyOrder(0, 1);
        assertThat(gd.getCardsExiledByPermanent(firstEyeId).contains(spell)
                || gd.getCardsExiledByPermanent(secondEyeId).contains(spell)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The caster can cast one copy while declining another")
    void canDeclineOnlySomeCopies() {
        UUID eyeId = addEye();
        FlowOfIdeas firstSpell = new FlowOfIdeas();
        harness.castFromHand(player1, firstSpell, "{5}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        TellingTime secondSpell = new TellingTime();
        harness.castFromHand(player1, secondSpell, "{1}{U}");
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        UUID selectedCopy = choice.validCopyIds().getFirst();
        UUID declinedCopy = choice.validCopyIds().get(1);

        harness.handleMultipleCardsChosen(player1, List.of(selectedCopy));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isTrue();
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(selectedCopy);
        assertThat(gd.findExiledCard(declinedCopy)).isNull();
        assertThat(gd.getCardsExiledByPermanent(eyeId)).containsExactly(firstSpell, secondSpell);
    }
}
