package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcadeGannon.class, DarksteelCitadel.class, DarksteelRelic.class, EliteVanguard.class,
        Forest.class, GrizzlyBears.class})
class ArcadeGannonTest extends BaseCardTest {

    @Test
    @DisplayName("The tap ability loots and puts a quest counter on Arcade Gannon")
    void lootsAndAddsQuestCounter() {
        Permanent arcade = addReadyArcade();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(arcade.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(arcade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A zero-mana artifact can be cast with no quest counters")
    void castsZeroManaArtifactAtZeroCounters() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("The permission casts a Human whose mana value equals the quest counter count")
    void castsHumanUpToQuestCounterCount() {
        Permanent arcade = addReadyArcade();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(arcade.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("Cards outside the artifact or Human mana-value limit cannot be cast")
    void rejectsCardsOutsideTheFilter() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new DarksteelCitadel()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The permission allows only one spell each turn")
    void rejectsSecondSpellInSameTurn() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setGraveyard(player1, List.of(new DarksteelRelic(), new DarksteelRelic()));
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("The permission does not apply during an opponent's turn")
    void rejectsCastingOnOpponentsTurn() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An eligible Human still requires its normal mana cost")
    void requiresManaAndDoesNotConsumePermissionOnFailedCast() {
        Permanent arcade = addReadyArcade();
        arcade.setCounterCount(CounterType.QUEST, 1);
        harness.setGraveyard(player1, List.of(new EliteVanguard()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("An eligible Human above the quest counter limit cannot be cast")
    void rejectsHumanAboveQuestCounterCount() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setGraveyard(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enough quest counters do not permit a nonartifact non-Human creature")
    void rejectsNonHumanWithEnoughQuestCountersAndMana() {
        Permanent arcade = addReadyArcade();
        arcade.setCounterCount(CounterType.QUEST, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard casting obeys normal artifact timing")
    void rejectsArtifactDuringUpkeep() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard casting permission resets on the next controller turn")
    void castsAgainOnNextTurn() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new DarksteelRelic(), new DarksteelRelic()));
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof DarksteelRelic)
                .hasSize(2);
        harness.assertNotInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void rejectsTapAbilityWhileSummoningSick() {
        harness.addToBattlefield(player1, new ArcadeGannon());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArcade() {
        Permanent arcade = harness.addToBattlefieldAndReturn(player1, new ArcadeGannon());
        arcade.setSummoningSick(false);
        return arcade;
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
