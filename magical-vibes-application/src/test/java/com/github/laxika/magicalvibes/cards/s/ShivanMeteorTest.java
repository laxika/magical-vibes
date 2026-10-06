package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanMeteor.class, BloodKnight.class, UrborgTombOfYawgmoth.class})
class ShivanMeteorTest extends BaseCardTest {

    @Test
    void dealsThirteenDamageToTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BloodKnight()).getId();
        harness.setHand(player1, List.of(new ShivanMeteor()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Blood Knight");
        harness.assertInGraveyard(player2, "Blood Knight");
    }

    @Test
    void cannotTargetNonCreature() {
        ShivanMeteor card = new ShivanMeteor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth()).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void suspendExilesWithTwoTimeCounters() {
        ShivanMeteor card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    void suspendedSpellCanBeCastForFreeAfterLastCounterIsRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BloodKnight()).getId();
        ShivanMeteor card = suspendCard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blood Knight");
        harness.assertInGraveyard(player1, "Shivan Meteor");
    }

    @Test
    void suspendCanOnlyBeActivatedAtSorcerySpeed() {
        ShivanMeteor card = new ShivanMeteor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void suspendRemovesCountersOnlyDuringOwnersUpkeep() {
        ShivanMeteor card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void decliningSuspendCastLeavesCardExiledWithoutCounters() {
        harness.addToBattlefield(player2, new BloodKnight());
        ShivanMeteor card = suspendCard();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertOnBattlefield(player2, "Blood Knight");
        harness.assertNotInGraveyard(player1, "Shivan Meteor");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void canTargetCreatureControlledByCaster() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();
        harness.setHand(player1, List.of(new ShivanMeteor()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Blood Knight");
        harness.assertInGraveyard(player1, "Blood Knight");
        harness.assertInGraveyard(player1, "Shivan Meteor");
    }

    private ShivanMeteor suspendCard() {
        ShivanMeteor card = new ShivanMeteor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
