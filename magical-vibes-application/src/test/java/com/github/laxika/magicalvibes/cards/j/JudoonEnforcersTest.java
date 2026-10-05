package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JudoonEnforcers.class, GiantCockroach.class, PithingNeedle.class})
class JudoonEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("No more than one creature can attack its controller")
    void limitsAttacksAgainstController() {
        harness.addToBattlefield(player2, new JudoonEnforcers());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("One creature can attack its controller")
    void allowsOneAttackAgainstController() {
        harness.addToBattlefield(player2, new JudoonEnforcers());
        addCreatureReady(player1, new GiantCockroach());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Suspend exiles Judoon Enforcers with six time counters")
    void suspendExilesWithSixTimeCounters() {
        JudoonEnforcers card = new JudoonEnforcers();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanAttackWithMultipleCreatures() {
        addCreatureReady(player1, new JudoonEnforcers());
        addCreatureReady(player1, new JudoonEnforcers());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1)))
                .doesNotThrowAnyException();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JudoonEnforcers());
        Permanent blocker = addCreatureReady(player2, new GiantCockroach());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Giant Cockroach");
        harness.assertOnBattlefield(player1, "Judoon Enforcers");
    }

    @Test
    void opponentsUpkeepDoesNotRemoveTimeCounters() {
        JudoonEnforcers card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void sixthUpkeepOffersFreeCastAndCreatureCanAttackImmediately() {
        JudoonEnforcers card = suspendCard();

        for (int remaining = 5; remaining > 0; remaining--) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
            assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), remaining);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
            harness.assertNotOnBattlefield(player1, "Judoon Enforcers");
        }

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Judoon Enforcers");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    void decliningSuspendCastLeavesCardExiledWithoutFurtherUpkeepTriggers() {
        JudoonEnforcers card = suspendCard();

        for (int upkeep = 0; upkeep < 6; upkeep++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Judoon Enforcers");
    }

    @Test
    void normalCastDoesNotGrantSuspendHaste() {
        harness.castFromHand(player1, new JudoonEnforcers(), "{5}{R}{W}");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Judoon Enforcers"), Keyword.HASTE))
                .isFalse();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSuspendDuringUpkeepWithoutFlashPermission() {
        JudoonEnforcers card = new JudoonEnforcers();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    void pithingNeedleDoesNotPreventSuspendSpecialAction() {
        harness.addToBattlefieldAndReturn(player2, new PithingNeedle())
                .setChosenName("Judoon Enforcers");

        JudoonEnforcers card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.stack).isEmpty();
    }

    private JudoonEnforcers suspendCard() {
        JudoonEnforcers card = new JudoonEnforcers();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
