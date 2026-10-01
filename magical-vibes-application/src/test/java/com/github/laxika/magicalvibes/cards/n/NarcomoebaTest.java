package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OglorDevotedAssistant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Narcomoeba.class, Millstone.class, OglorDevotedAssistant.class})
class NarcomoebaTest extends BaseCardTest {

    @Test
    @DisplayName("When milled, Narcomoeba may return itself from the graveyard to the battlefield")
    void mayReturnItselfFromGraveyardWhenMilled() {
        Card narcomoeba = setUpMillAndReturnCard();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(narcomoeba.getId()));
    }

    @Test
    @DisplayName("Declining Narcomoeba's mill trigger leaves it in the graveyard")
    void decliningMillTriggerLeavesItInGraveyard() {
        Card narcomoeba = setUpMillAndReturnCard();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(narcomoeba.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
    }

    @Test
    @DisplayName("When put into its graveyard from its library without being milled, Narcomoeba may return itself")
    void mayReturnItselfWhenPutIntoGraveyardFromLibraryWithoutBeingMilled() {
        Card narcomoeba = new Narcomoeba();
        harness.addToBattlefield(player1, new OglorDevotedAssistant());
        harness.setLibrary(player1, List.of(narcomoeba, new Narcomoeba()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(narcomoeba.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(narcomoeba.getId()));
    }

    private Card setUpMillAndReturnCard() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        millstone.setSummoningSick(false);

        Card narcomoeba = new Narcomoeba();
        harness.setLibrary(player1, List.of(narcomoeba));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        return narcomoeba;
    }
}
