package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MishrasResearchDesk.class, Forest.class})
class MishrasResearchDeskTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability exiles two cards and grants play permission to the chosen card")
    void sacrificeAbilityExilesTwoCardsAndGrantsChosenCardPermission() {
        Card first = new MishrasResearchDesk();
        Card second = new Forest();
        Card third = new MishrasResearchDesk();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player1, new MishrasResearchDesk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Mishra's Research Desk");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(second.getId(), player1.getId())
                .doesNotContainKey(first.getId());
    }

    @Test
    @DisplayName("Unearth returns the desk with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new MishrasResearchDesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent desk = findPermanent(player1, "Mishra's Research Desk");
        assertThat(desk.getGrantedKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mishra's Research Desk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mishra's Research Desk"));
    }

    @Test
    @DisplayName("The chosen land can be played, but the unchosen land cannot")
    void onlyChosenLandCanBePlayed() {
        Card chosen = new Forest();
        Card unchosen = new Forest();
        harness.setLibrary(player1, List.of(chosen, unchosen));
        harness.addToBattlefield(player1, new MishrasResearchDesk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, unchosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, chosen.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(unchosen);
    }

    @Test
    @DisplayName("Playing the chosen spell still requires paying its mana cost")
    void chosenSpellRequiresManaCost() {
        Card chosen = new MishrasResearchDesk();
        harness.setLibrary(player1, List.of(chosen));
        harness.addToBattlefield(player1, new MishrasResearchDesk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mishra's Research Desk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not require a card choice")
    void emptyLibraryDoesNotRequireChoice() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new MishrasResearchDesk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mishra's Research Desk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing an unearthed desk exiles it and still resolves its ability")
    void unearthedDeskCanBeSacrificedForItsAbility() {
        Card desk = new MishrasResearchDesk();
        Card chosen = new Forest();
        harness.setGraveyard(player1, List.of(desk));
        harness.setLibrary(player1, List.of(chosen));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Mishra's Research Desk");
        harness.assertNotInGraveyard(player1, "Mishra's Research Desk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(desk);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.castFromExile(player1, chosen.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(desk);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new MishrasResearchDesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mishra's Research Desk");
        harness.assertNotOnBattlefield(player1, "Mishra's Research Desk");
    }

    @Test
    @DisplayName("Play permission lasts through the next turn's end step and then expires")
    void playPermissionExpiresAfterNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Card chosen = new Forest();
        harness.setLibrary(player1, List.of(chosen, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new MishrasResearchDesk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
    }
}
