package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostIntercessor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HangarScrounger.class, AlabasterHostIntercessor.class, Island.class})
class HangarScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a counter on another creature and grants the tapped rummage ability")
    void backsUpAnotherCreature() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);

        Card discarded = new AlabasterHostIntercessor();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(recipient);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Backup targeting itself leaves exactly one printed tapped rummage ability")
    void backingUpItselfRetainsPrintedAbility() {
        Permanent scrounger = castScrounger();
        resolveEtbTargeting(scrounger);
        assertThat(scrounger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Card discarded = new Island();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        tapAndResolve(scrounger);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted tapped rummage ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        recipient.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, recipient));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The original Scrounger still rummages after backing up another creature")
    void originalCreatureRetainsPrintedAbility() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        Permanent scrounger = castScrounger();
        resolveEtbTargeting(recipient);
        Card discarded = new Island();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(scrounger);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the granted ability neither discards nor draws")
    void mayDeclineRummage() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);
        Card kept = new Island();
        Card undrawn = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));

        tapAndResolve(recipient);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw a card")
    void cannotDrawWithoutDiscarding() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);
        harness.setHand(player1, List.of());
        Card undrawn = new Island();
        harness.setLibrary(player1, List.of(undrawn));

        tapAndResolve(recipient);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Backup can grant the ability to an opponent's creature for that opponent")
    void opponentControlsGrantedAbility() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);
        Card discarded = new Island();
        Card drawn = new Island();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));

        tapAndResolve(recipient);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping another ally does not trigger the recipient's granted ability")
    void unrelatedCreatureDoesNotTriggerAbility() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);

        unrelated.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, unrelated));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The granted ability triggers again when the creature is untapped and tapped again")
    void grantedAbilityCanTriggerMultipleTimes() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        castScrounger();
        resolveEtbTargeting(recipient);
        Card firstDiscard = new Island();
        Card firstDraw = new Island();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(firstDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        tapAndResolve(recipient);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        recipient.untap();
        tapAndResolve(recipient);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, firstDraw);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent castScrounger() {
        harness.castFromHand(player1, new HangarScrounger(), "{2}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Hangar Scrounger");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.passBothPriorities();
    }
}
