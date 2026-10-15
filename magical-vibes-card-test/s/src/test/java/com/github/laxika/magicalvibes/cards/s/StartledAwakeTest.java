package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DauntlessCathar;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StartledAwake.class, Forest.class, DauntlessCathar.class, SeagrafSkaab.class})
class StartledAwakeTest extends BaseCardTest {

    @Test
    @DisplayName("Mills thirteen cards from the targeted opponent")
    void millsThirteenCardsFromTargetOpponent() {
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()
        ));
        harness.setHand(player1, List.of(new StartledAwake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(13);
    }

    @Test
    @DisplayName("Returns from the graveyard transformed as Persistent Nightmare")
    void returnsFromGraveyardTransformed() {
        Permanent nightmare = returnNightmare();
        assertThat(nightmare.isTransformed()).isTrue();
        assertThat(nightmare.isTapped()).isFalse();
        assertThat(nightmare.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Startled Awake");
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated outside sorcery timing")
    void graveyardAbilityRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Persistent Nightmare returns to its owner's hand after combat damage to a player")
    void persistentNightmareReturnsToHandAfterCombatDamage() {
        Permanent nightmare = returnNightmare();
        nightmare.setSummoningSick(false);
        nightmare.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Startled Awake");
        harness.assertNotOnBattlefield(player1, "Persistent Nightmare");
        harness.assertLife(player2, 19);
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new StartledAwake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsAllCardsWhenLibraryHasFewerThanThirteen() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new StartledAwake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Startled Awake");
    }

    @Test
    void graveyardAbilityRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.setHand(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.castSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardAbilityRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardAbilityRequiresTwoBlueMana() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void skulkPreventsGreaterPowerBlocker() {
        Permanent nightmare = returnNightmare();
        nightmare.setSummoningSick(false);
        addCreatureReady(player2, new DauntlessCathar());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    void equalPowerBlockerIsLegalAndCreatureDamageDoesNotReturnNightmare() {
        Permanent nightmare = returnNightmare();
        nightmare.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new SeagrafSkaab());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Startled Awake");
        harness.assertNotInHand(player1, "Startled Awake");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotReturnIfSourceLeavesGraveyardBeforeResolution() {
        StartledAwake card = new StartledAwake();
        harness.setGraveyard(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);

        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
        harness.setHand(player1, List.of(card));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Persistent Nightmare");
        harness.assertInHand(player1, "Startled Awake");
    }

    @Test
    void doesNotReturnIfSourceLeavesAndReentersGraveyardBeforeResolution() {
        StartledAwake card = new StartledAwake();
        harness.setGraveyard(player1, List.of(card));
        gd.markGraveyardEntry(card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);

        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
        harness.setHand(player1, List.of(card));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        gd.markGraveyardEntry(card);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Persistent Nightmare");
        harness.assertInGraveyard(player1, "Startled Awake");
    }

    private Permanent returnNightmare() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Persistent Nightmare");
    }

    @Test
    @CardUsed({Moonmist.class, MaskwoodNexus.class})
    void ignoresInstructionToTransformIntoSorcery() {
        Permanent nightmare = returnNightmare();
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Persistent Nightmare");
        assertThat(nightmare.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Startled Awake");
    }
}
