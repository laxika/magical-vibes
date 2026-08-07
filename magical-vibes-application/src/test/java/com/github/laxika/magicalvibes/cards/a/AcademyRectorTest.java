package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FeedThePack;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AcademyRectorTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, accept may: exiles Rector, search offers only enchantments, chosen one enters the battlefield")
    void diesAcceptMaySearchChoosesEnchantment() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        setupLibrary(player1);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Rector dies, ON_DEATH trigger on stack
        harness.passBothPriorities(); // trigger resolves → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // accept → exile + search

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(rectorCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.ENCHANTMENT));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Feed the Pack");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.ENCHANTMENT));
    }

    @Test
    @DisplayName("Dies, decline may: Rector stays in graveyard, no search")
    void diesDeclineMayKeepsRectorInGraveyard() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        setupLibrary(player1);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Rector dies
        harness.passBothPriorities(); // trigger resolves → may prompt

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Feed the Pack");
    }

    @Test
    @DisplayName("Dies, accept may, no enchantments in library: Rector exiled, search finds nothing")
    void diesAcceptMayFailToFind() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Rector dies
        harness.passBothPriorities(); // trigger resolves → may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.ENCHANTMENT));
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new FeedThePack(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
