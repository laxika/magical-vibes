package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerazolTheSplitCurrent.class, LlanowarElite.class, Cancel.class})
class VerazolTheSplitCurrentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each mana spent to cast it")
    void entersWithCountersForManaSpent() {
        castVerazol();

        Permanent verazol = findPermanent(player1, "Verazol, the Split Current");
        assertThat(verazol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing two counters copies a kicked permanent spell as a token")
    void mayRemoveTwoCountersToCopyKickedPermanentSpellAsToken() {
        castVerazol();
        castKickedElite();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        List<Permanent> elites = findPermanents(player1, "Llanowar Elite");
        assertThat(elites).hasSize(2);
        assertThat(elites).anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining removes no counters and copies nothing")
    void decliningDoesNotRemoveCountersOrCopySpell() {
        castVerazol();
        castKickedElite();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Llanowar Elite")).hasSize(1);
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("\"If you do, copy that spell\": the copy is made as the trigger resolves, not by a reflexive ability")
    void createsCopyDuringCounterRemovalAbilityResolution() {
        castVerazol();
        castKickedElite();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Llanowar Elite"));
    }

    @Test
    @DisplayName("The copy of a kicked creature spell is kicked too")
    void copiedPermanentRetainsKickedEntryCounters() {
        castVerazol();
        castKickedElite();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Llanowar Elite"))
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent
                        .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5));
    }

    @Test
    @DisplayName("With one counter, accepting removes none and copies nothing")
    void insufficientCountersCannotPayForCopy() {
        castVerazol();
        findPermanent(player1, "Verazol, the Split Current")
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castKickedElite();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Llanowar Elite")).hasSize(1);
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unkicked spell doesn't trigger the ability")
    void unkickedSpellDoesNotTriggerCopyAbility() {
        castVerazol();
        harness.setHand(player1, List.of(new LlanowarElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Llanowar Elite")).hasSize(1);
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("With X of 0 it still counts the colored mana spent")
    void zeroXStillCountsTheColoredManaSpent() {
        harness.setHand(player1, List.of(new VerazolTheSplitCurrent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Copies the kicked spell even if it was countered before the ability resolves")
    void copiesTheKickedSpellEvenIfItWasCounteredBeforeTheAbilityResolves() {
        castVerazol();
        LlanowarElite elite = new LlanowarElite();
        harness.setHand(player1, List.of(elite));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castKickedCreature(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, elite.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elite);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.passBothPriorities();

        List<Permanent> elites = findPermanents(player1, "Llanowar Elite");
        assertThat(elites).hasSize(1);
        assertThat(elites.getFirst().getCard().isToken()).isTrue();
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castVerazol() {
        harness.setHand(player1, List.of(new VerazolTheSplitCurrent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, 1);
        resolveAllTriggers();
    }

    private void castKickedElite() {
        harness.setHand(player1, List.of(new LlanowarElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castKickedCreature(player1, 0);
    }
}
