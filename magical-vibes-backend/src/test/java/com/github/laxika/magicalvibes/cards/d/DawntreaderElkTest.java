package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DawntreaderElkTest extends BaseCardTest {

    @Test
    @DisplayName("Dawntreader Elk is sacrificed as part of the cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dawntreader Elk"));
    }

    @Test
    @DisplayName("Activating the ability searches for a basic land that enters tapped")
    void searchPutsBasicLandTappedOntoBattlefield() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → library search

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With no basic lands in library, no land enters the battlefield")
    void failToFindNoBasicLand() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → no basic land to find

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Cannot activate the ability without green mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        setupLibrary(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
