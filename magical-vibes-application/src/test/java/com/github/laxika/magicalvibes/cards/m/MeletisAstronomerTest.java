package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MeletisAstronomerTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic reveals an enchantment into hand and puts the rest on the library bottom")
    void heroicFindsEnchantmentAmongTopThree() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        Card instant = new Shock();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(enchantment, instant, land));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castInstant(player1, 0, astronomerId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(enchantment);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant, land);
    }

    @Test
    @DisplayName("Declining the heroic choice leaves the matching card on the library bottom")
    void mayDeclineEnchantmentReveal() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castInstant(player1, 0, astronomerId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }

    @Test
    @DisplayName("An opponent's spell targeting the creature does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castInstant(player2, 0, astronomerId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }
}
