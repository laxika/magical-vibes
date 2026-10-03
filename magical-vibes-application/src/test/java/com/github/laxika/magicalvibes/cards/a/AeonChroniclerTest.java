package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AeonChronicler.class, TeferiMageOfZhalfir.class})
class AeonChroniclerTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualCardsInControllerHand() {
        Permanent chronicler = harness.addToBattlefieldAndReturn(player1, new AeonChronicler());
        harness.setHand(player1, List.of(new AeonChronicler(), new AeonChronicler(), new AeonChronicler()));

        assertThat(gqs.getEffectivePower(gd, chronicler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isEqualTo(3);
    }

    @Test
    void powerAndToughnessUpdateWithControllerHandSize() {
        Permanent chronicler = harness.addToBattlefieldAndReturn(player1, new AeonChronicler());

        harness.setHand(player1, List.of(new AeonChronicler()));
        assertThat(gqs.getEffectivePower(gd, chronicler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isEqualTo(1);

        harness.setHand(player1, List.of(new AeonChronicler(), new AeonChronicler()));
        assertThat(gqs.getEffectivePower(gd, chronicler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isEqualTo(2);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, chronicler)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isZero();
    }

    @Test
    void powerAndToughnessIgnoreOpponentsHand() {
        Permanent chronicler = harness.addToBattlefieldAndReturn(player1, new AeonChronicler());
        harness.setHand(player1, List.of(new AeonChronicler()));
        harness.setHand(player2, List.of(new AeonChronicler(), new AeonChronicler(), new AeonChronicler()));

        assertThat(gqs.getEffectivePower(gd, chronicler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isEqualTo(1);
    }

    @Test
    void suspendUsesChosenXAsTimeCounters() {
        AeonChronicler card = suspendCard(3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void suspendXCannotBeZero() {
        AeonChronicler card = new AeonChronicler();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void removingTimeCounterDrawsACard() {
        AeonChronicler card = suspendCard(2);
        int handSizeBeforeUpkeep = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeUpkeep + 1);
    }

    @Test
    void lastTimeCounterDrawsACardAndOffersFreeCast() {
        AeonChronicler card = suspendCard(1);
        int handSizeBeforeUpkeep = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeUpkeep + 1);
    }

    @Test
    void lastTimeCounterCanCastCardWithoutPayingMana() {
        AeonChronicler card = suspendCard(1);
        harness.setHand(player1, List.of(new AeonChronicler()));
        harness.setLibrary(player1, List.of(new AeonChronicler()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aeon Chronicler");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void powerAndToughnessInHandIncludeTheChroniclerItself() {
        AeonChronicler card = new AeonChronicler();
        harness.setHand(player1, List.of(card, new AeonChronicler(), new AeonChronicler()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);
    }

    @Test
    void powerAndToughnessInGraveyardTrackOwnersHand() {
        AeonChronicler card = new AeonChronicler();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new AeonChronicler(), new AeonChronicler()));
        harness.setHand(player2, List.of(new AeonChronicler()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);

        harness.setHand(player1, List.of(new AeonChronicler()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);
    }

    @Test
    void powerAndToughnessWhileSuspendedTrackOwnersHand() {
        AeonChronicler card = suspendCard(2);
        harness.setHand(player1, List.of(new AeonChronicler(), new AeonChronicler()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
    }

    @Test
    void suspendIsASpecialActionAndPaysTheFullChosenCost() {
        suspendCard(3);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void suspendCanBeUsedDuringOpponentsUpkeepWhenGrantedFlash() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        AeonChronicler card = suspendCard(2);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsUpkeepDoesNotRemoveATimeCounterOrDraw() {
        AeonChronicler card = suspendCard(2);
        harness.setLibrary(player1, List.of(new AeonChronicler()));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lastCounterCreatesSeparateRespondableDrawAndCastTriggers() {
        suspendCard(1);
        harness.setLibrary(player1, List.of(new AeonChronicler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandCausesChroniclerToDie() {
        harness.setHand(player1, List.of(new AeonChronicler()));
        harness.addToBattlefield(player1, new AeonChronicler());
        harness.setHand(player1, List.of());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Aeon Chronicler");
        harness.assertInGraveyard(player1, "Aeon Chronicler");
    }

    @Test
    void creatureCastFromSuspendCanAttackImmediately() {
        suspendCard(1);
        harness.setHand(player1, List.of(new AeonChronicler()));
        harness.setLibrary(player1, List.of(new AeonChronicler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Aeon Chronicler").isAttacking()).isTrue();
    }

    @Test
    void decliningSuspendCastLeavesCardExiledWithoutFurtherUpkeepTriggers() {
        AeonChronicler card = suspendCard(1);
        harness.setLibrary(player1, List.of(new AeonChronicler(), new AeonChronicler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private AeonChronicler suspendCard(int xValue) {
        AeonChronicler card = new AeonChronicler();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 3);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }
}
