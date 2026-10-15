package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomsTimePlatform.class, Forest.class, GrizzlyBears.class})
class DoomsTimePlatformTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles a target nonland card from your graveyard with two time counters")
    void attackingExilesTargetNonlandCardFromYourGraveyard() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(land, nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(nonland.getId());

        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(nonland.getId());
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 2);
    }

    @Test
    @DisplayName("The suspended card counts down and can be cast for free")
    void suspendedCardCanBeCastForFree() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));
        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land is not a legal target")
    void landCannotBeTargeted() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("The required graveyard target cannot be declined")
    void cannotDeclineRequiredTarget() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Several attackers produce only one exile trigger")
    void severalAttackersExileOnlyOneCard() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        addAttackTriggerSourceAndAttacker();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(second.getId());
    }

    @Test
    @DisplayName("An opponent's graveyard is not eligible")
    void opponentGraveyardCannotBeTargeted() {
        GrizzlyBears own = new GrizzlyBears();
        GrizzlyBears opposing = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opposing));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(own.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposing.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(own.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent upkeep does not count down your suspended card")
    void onlyOwnerUpkeepRemovesTimeCounter() {
        GrizzlyBears card = new GrizzlyBears();
        suspendFromGraveyard(card);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
    }

    @Test
    @DisplayName("Declining suspend leaves the card exiled without another offer")
    void decliningCastLeavesCardExiled() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        suspendFromGraveyard(card);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(card.getId());
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    @DisplayName("Removing the last time counter creates a separate respondable trigger")
    void lastCounterCastAbilityUsesStack() {
        GrizzlyBears card = new GrizzlyBears();
        suspendFromGraveyard(card);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(card.getId());
        });
    }

    @Test
    @DisplayName("A creature cast through suspend retains haste after cleanup")
    void suspendHasteSurvivesCleanup() {
        GrizzlyBears card = new GrizzlyBears();
        suspendFromGraveyard(card);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent cast = findBattlefieldPermanent(player1, card);
        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Suspend can cast a noncreature artifact as well")
    void suspendedArtifactCanBeCast() {
        DoomsTimePlatform card = new DoomsTimePlatform();
        suspendFromGraveyard(card);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Doom's Time Platform")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void suspendFromGraveyard(Card card) {
        harness.setGraveyard(player1, List.of(card));
        addAttackTriggerSourceAndAttacker();
        declareAttackers(List.of(1));
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The Platform does not trigger when an opponent attacks")
    void opponentAttackDoesNotTrigger() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.addToBattlefield(player1, new DoomsTimePlatform());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is not suspended")
    void departedTargetIsNotSuspended() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        addAttackTriggerSourceAndAttacker();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
            harness.setGraveyard(player1, List.of());
            harness.setHand(player1, List.of(card));
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    private Permanent findBattlefieldPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void addAttackTriggerSourceAndAttacker() {
        harness.addToBattlefield(player1, new DoomsTimePlatform());
        addCreatureReady(player1, new GrizzlyBears());
    }
}
