package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftwingCloudskate.class, Island.class})
class RiftwingCloudskateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns target permanent to its owner's hand")
    void etbBouncesTargetPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player1, List.of(new RiftwingCloudskate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertOnBattlefield(player1, "Riftwing Cloudskate");
    }

    @Test
    @DisplayName("Suspend exiles Riftwing Cloudskate with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        RiftwingCloudskate card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend counters are removed only during the owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        RiftwingCloudskate card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        RiftwingCloudskate card = suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Riftwing Cloudskate");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Riftwing Cloudskate in exile")
    void decliningSuspendCastLeavesCardInExile() {
        RiftwingCloudskate card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Riftwing Cloudskate");
    }

    @Test
    @DisplayName("Suspend can cast onto an empty battlefield and the enters trigger can target itself")
    void suspendCanCastOntoEmptyBattlefieldAndBounceItself() {
        RiftwingCloudskate card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(card));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);

        harness.passBothPriorities();
        UUID selfId = harness.getPermanentId(player1, "Riftwing Cloudskate");
        harness.handlePermanentChosen(player1, selfId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riftwing Cloudskate");
        harness.assertInHand(player1, "Riftwing Cloudskate");
    }

    @Test
    @DisplayName("Each owner's upkeep removes exactly one time counter without casting early")
    void ownerUpkeepsRemoveOneCounterAtATime() {
        RiftwingCloudskate card = suspendCard();

        for (int remaining = 2; remaining >= 1; remaining--) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), remaining);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            harness.assertNotOnBattlefield(player1, "Riftwing Cloudskate");
        }
    }

    @Test
    @DisplayName("The enters trigger can return a permanent controlled by its controller")
    void etbCanBounceOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.setHand(player1, List.of(new RiftwingCloudskate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Riftwing Cloudskate");
    }

    @Test
    @DisplayName("Suspend cannot be used during the opponent's turn")
    void cannotSuspendDuringOpponentsTurn() {
        RiftwingCloudskate card = new RiftwingCloudskate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    @DisplayName("Declining suspend does not offer another cast on later upkeeps")
    void declinedSuspendDoesNotTriggerAgain() {
        RiftwingCloudskate card = suspendCard();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Riftwing Cloudskate");
    }

    private RiftwingCloudskate suspendCard() {
        RiftwingCloudskate card = new RiftwingCloudskate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
