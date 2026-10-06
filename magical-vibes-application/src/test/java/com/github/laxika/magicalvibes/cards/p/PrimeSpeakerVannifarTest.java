package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimeSpeakerVannifar.class, LlanowarElves.class, GoldMyr.class, HillGiant.class})
class PrimeSpeakerVannifarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice a creature and search for a creature with exactly one higher mana value")
    void sacrificeCreatureSearchesForCreatureWithOneHigherManaValue() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GoldMyr(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Gold Myr"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Cannot sacrifice Prime Speaker Vannifar itself")
    void cannotSacrificeSourceItself() {
        addVannifarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addVannifarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeAndTapArePaidBeforeResolution() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GoldMyr()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Gold Myr");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gold Myr");
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isFalse();
    }

    @Test
    void canFailToFindEvenWhenMatchingCreatureExists() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GoldMyr()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Gold Myr");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoMatchingCreatureStillFinishes() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GoldMyr()));
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).getFirst().untap();
        addCreature(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new PrimeSpeakerVannifar()).setSummoningSick(true);
        addCreature(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void cannotActivateWhileTapped() {
        addVannifarReady(player1);
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        addCreature(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    private void addVannifarReady(Player player) {
        harness.addToBattlefieldAndReturn(player, new PrimeSpeakerVannifar()).setSummoningSick(false);
    }

    private void addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefieldAndReturn(player, card).setSummoningSick(false);
    }
}
