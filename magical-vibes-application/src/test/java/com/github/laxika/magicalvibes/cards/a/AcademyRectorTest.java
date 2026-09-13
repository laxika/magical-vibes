package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcademyRector.class, Compost.class, MetathranSoldier.class, RecklessAbandon.class})
class AcademyRectorTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, accept may: exiles Rector, search offers only enchantments, chosen one enters the battlefield")
    void diesAcceptMaySearchChoosesEnchantment() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        castRecklessAbandonAt(player1, rector);

        setupLibrary(player1);

        harness.passBothPriorities(); // Reckless Abandon resolves — Rector dies and its trigger is on the stack.
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

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Compost");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.ENCHANTMENT));
    }

    @Test
    @DisplayName("Dies, decline may: Rector stays in graveyard, no search")
    void diesDeclineMayKeepsRectorInGraveyard() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        castRecklessAbandonAt(player1, rector);

        setupLibrary(player1);

        harness.passBothPriorities(); // Reckless Abandon resolves — Rector dies
        harness.passBothPriorities(); // trigger resolves → may prompt

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Compost");
    }

    @Test
    @DisplayName("Dies, accept may, no enchantments in library: Rector exiled, search finds nothing")
    void diesAcceptMayFailToFind() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        castRecklessAbandonAt(player1, rector);

        harness.setLibrary(player1, List.of(new MetathranSoldier(), new MetathranSoldier()));

        harness.passBothPriorities(); // Reckless Abandon resolves — Rector dies
        harness.passBothPriorities(); // trigger resolves → may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(rectorCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.ENCHANTMENT));
    }

    @Test
    @DisplayName("If Rector leaves its graveyard before the trigger resolves, it cannot search")
    void doesNotSearchIfRectorLeavesGraveyardBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new AcademyRector());
        Permanent rector = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card rectorCard = rector.getCard();

        castRecklessAbandonAt(player1, rector);
        setupLibrary(player1);

        harness.passBothPriorities(); // Reckless Abandon resolves — Rector dies and its trigger is on the stack.

        gd.playerGraveyards.get(player1.getId())
                .removeIf(card -> card.getId().equals(rectorCard.getId()));
        harness.setExile(player1, List.of(rectorCard));

        harness.passBothPriorities(); // Trigger resolves and prompts for the may choice.
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Compost");
    }

    private void castRecklessAbandonAt(com.github.laxika.magicalvibes.model.Player player, Permanent rector) {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player, new MetathranSoldier());
        harness.setHand(player, List.of(new RecklessAbandon()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, rector.getId(), sacrifice.getId());
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        harness.setLibrary(player, List.of(new Compost(), new MetathranSoldier(), new MetathranSoldier()));
    }
}
