package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moggcatcher.class, MoggToady.class, MoggAlarm.class})
class MoggcatcherTest extends BaseCardTest {

    private void setUpMoggcatcher() {
        addCreatureReady(player1, new Moggcatcher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Only Goblin permanent cards are offered by the search")
    void searchOffersOnlyGoblinPermanents() {
        setUpMoggcatcher();
        harness.setLibrary(player1, List.of(new MoggToady(), new MoggAlarm(), new Moggcatcher()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Mogg Toady");
    }

    @Test
    @DisplayName("The chosen Goblin permanent enters the battlefield")
    void chosenGoblinPermanentEntersBattlefield() {
        setUpMoggcatcher();
        harness.setLibrary(player1, List.of(new MoggToady()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mogg Toady");
        assertThat(findPermanent(player1, "Moggcatcher").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Finding no Goblin permanent leaves the battlefield unchanged")
    void noGoblinPermanentFound() {
        setUpMoggcatcher();
        harness.setLibrary(player1, List.of(new MoggAlarm(), new Moggcatcher()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without three mana")
    void activationRequiresThreeMana() {
        addCreatureReady(player1, new Moggcatcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Moggcatcher").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
