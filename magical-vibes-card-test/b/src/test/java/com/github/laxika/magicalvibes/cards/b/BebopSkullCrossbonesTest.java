package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RocksteadyMutantMarauder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BebopSkullCrossbones.class, Forest.class, RocksteadyMutantMarauder.class})
class BebopSkullCrossbonesTest extends BaseCardTest {

    @Test
    @DisplayName("Target player may search for Rocksteady and put it into their hand")
    void targetPlayerMaySearchForPartner() {
        Card partner = new RocksteadyMutantMarauder();
        Forest decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.setHand(player1, List.of(new BebopSkullCrossbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Rocksteady, Mutant Marauder");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Rocksteady, Mutant Marauder");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("Combat damage may draw and lose life based on Bebop's counters")
    void combatDamageDrawsAndLosesLifePerCounter() {
        Permanent bebop = addCreatureReady(player1, new BebopSkullCrossbones());
        bebop.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bebop.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger does nothing")
    void decliningCombatDamageTriggerDoesNothing() {
        Permanent bebop = addCreatureReady(player1, new BebopSkullCrossbones());
        bebop.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bebop.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void acceptingPartnerSearchWithoutPartnerFinishesWithoutAddingCards() {
        Card decoy = new BebopSkullCrossbones();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy));
        harness.setHand(player1, List.of(new BebopSkullCrossbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card partner = new RocksteadyMutantMarauder();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(partner));
        harness.setHand(player1, List.of(new BebopSkullCrossbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(partner);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllerCanSearchOwnLibraryAndFailToFindExistingPartner() {
        Card partner = new RocksteadyMutantMarauder();
        harness.setLibrary(player1, List.of(partner));
        harness.setHand(player1, List.of(new BebopSkullCrossbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(partner);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingWithNoCountersDrawsNothingAndLosesNoLife() {
        Permanent bebop = addCreatureReady(player1, new BebopSkullCrossbones());
        bebop.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void countsAllCounterTypesAtResolutionRatherThanWhenDamageWasDealt() {
        Permanent bebop = addCreatureReady(player1, new BebopSkullCrossbones());
        bebop.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bebop.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        harness.resolveCombatDamage();
        bebop.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bebop.setCounterCount(CounterType.VIGILANCE, 1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 17);
    }

    @Test
    void usesLastKnownCountersWhenBebopLeavesBeforeTriggerResolves() {
        Permanent bebop = addCreatureReady(player1, new BebopSkullCrossbones());
        bebop.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bebop.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bebop));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }
}
