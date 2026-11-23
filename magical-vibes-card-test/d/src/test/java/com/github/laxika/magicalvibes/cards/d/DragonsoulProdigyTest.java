package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsoulProdigy.class, DirgurIslandDragon.class, ThayanEvokers.class, Clone.class})
class DragonsoulProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("The first Omen spell conjures a hasty duplicate that is shuffled away at the next end step")
    void firstOmenSpellConjuresHastyDuplicateUntilNextEndStep() {
        harness.setHand(player1, List.of(new DragonsoulProdigy(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        Permanent duplicate = findPermanent(player1, "Dirgur Island Dragon");
        assertThat(duplicate.getCard().isToken()).isFalse();
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
        var duplicateCardId = duplicate.getCard().getId();

        advanceToNextEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(duplicateCardId));
    }

    @Test
    @DisplayName("Triggers only for the first Omen spell each turn")
    void triggersOnlyOnceForOmenSpellsEachTurn() {
        harness.setHand(player1, List.of(
                new DragonsoulProdigy(), new DirgurIslandDragon(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        assertThat(countPermanents(player1, "Dirgur Island Dragon")).isEqualTo(1);
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveStack();
    }

    @Test
    @DisplayName("Conjuring the duplicate triggers conjure abilities")
    void conjuringTheOmenDuplicateTriggersConjureAbilities() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.setHand(player1, List.of(new DragonsoulProdigy(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        assertThat(findPermanent(player1, "Dirgur Island Dragon")).isNotNull();
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("The duplicate's haste is gained, so a copy of it doesn't have haste")
    void copyOfTheDuplicateDoesNotCopyHaste() {
        harness.setHand(player1, List.of(new DragonsoulProdigy(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();
        Permanent duplicate = findPermanent(player1, "Dirgur Island Dragon");
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, duplicate.getId());
        resolveStack();

        Permanent copy = findPermanents(player1, "Dirgur Island Dragon").stream()
                .filter(permanent -> !permanent.getId().equals(duplicate.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The conjure of the duplicate is logged")
    void conjureIsLogged() {
        harness.setHand(player1, List.of(new DragonsoulProdigy(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        assertThat(gameLogContains("conjures a duplicate of Dirgur Island Dragon onto the battlefield.")).isTrue();
    }
}
