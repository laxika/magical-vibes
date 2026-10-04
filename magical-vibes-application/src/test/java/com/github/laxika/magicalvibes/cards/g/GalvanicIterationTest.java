package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CollectiveDefiance;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalvanicIteration.class, LightningBolt.class, CollectiveDefiance.class})
class GalvanicIterationTest extends BaseCardTest {

    @Test
    @DisplayName("Sets up a pending copy of the next instant or sorcery cast this turn")
    void setsUpPendingCopy() {
        harness.setHand(player1, List.of(new GalvanicIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Galvanic Iteration");
    }

    @Test
    @DisplayName("The next instant cast is copied, and only the first one")
    void copiesNextInstantOnly() {
        harness.setHand(player1, List.of(new GalvanicIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        // Resolve the copy trigger, which creates the copy and offers a retarget choice.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the copy, then the original Lightning Bolt.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Flashback casts from the graveyard for {1}{U}{R} and exiles the card")
    void flashbackSetsUpCopyAndExiles() {
        harness.setGraveyard(player1, List.of(new GalvanicIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Galvanic Iteration");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> "Galvanic Iteration".equals(entry.card().getName()));
    }

    @Test
    @DisplayName("The copy may target a different player without changing the original")
    void mayRetargetCopy() {
        harness.setHand(player1, List.of(new GalvanicIteration(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Copies a modal sorcery with its chosen mode")
    void copiesSorceryWithChosenMode() {
        harness.setHand(player1, List.of(new GalvanicIteration(), new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0);

        harness.castModalSorcery(player1, 0, 2, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Collective Defiance");
    }

    @Test
    @DisplayName("Copying a flashback Galvanic Iteration creates two delayed copies")
    void copiesUntargetedFlashbackAndStacksDelayedCopies() {
        harness.setHand(player1, List.of(new GalvanicIteration(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Galvanic Iteration");
        assertThat(gd.exiledCards)
                .filteredOn(entry -> "Galvanic Iteration".equals(entry.card().getName()))
                .hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();

        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting a spell does not consume the delayed copy")
    void ignoresOpponentsSpell() {
        harness.setHand(player1, List.of(new GalvanicIteration(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An unused delayed copy expires at the end of the turn")
    void unusedCopyExpires() {
        harness.setHand(player1, List.of(new GalvanicIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second instant after the delayed copy is consumed resolves only once")
    void secondInstantIsNotCopied() {
        harness.setHand(player1, List.of(new GalvanicIteration(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }
}
