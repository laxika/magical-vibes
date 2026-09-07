package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKeyToTheVault.class, GrizzlyBears.class, Island.class, Forest.class})
class TheKeyToTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage looks at that many cards and exiles a chosen nonland card")
    void exilesChosenNonlandCardAfterLookingAtCombatDamageCount() {
        Card chosen = new GrizzlyBears();
        Card land = new Island();
        Card untouched = new Forest();
        stackTop(List.of(land, chosen, untouched));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, untouched);
    }

    @Test
    @DisplayName("The exiled card can be cast without paying its mana cost")
    void castsChosenExiledCardForFree() {
        Card chosen = new GrizzlyBears();
        Card land = new Island();
        stackTop(List.of(chosen, land));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .contains(chosen.getId());
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    private void addEquippedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent key = addCreatureReady(player1, new TheKeyToTheVault());
        key.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
    }

    private void stackTop(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
