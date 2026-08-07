package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SakuraTribeElderTest extends BaseCardTest {

    @Test
    @DisplayName("Sakura-Tribe Elder is sacrificed as part of the cost, with no mana paid")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("Activating the ability searches for a basic land that enters tapped")
    void searchPutsBasicLandTappedOntoBattlefield() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With no basic lands in library, no land enters the battlefield")
    void failToFindNoBasicLand() {
        harness.addToBattlefield(player1, new SakuraTribeElder());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → no basic land to find

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    private void setupLibrary(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
