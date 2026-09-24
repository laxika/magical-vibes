package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelshaperApprentice.class, VulshokMorningstar.class, CrazedGoblin.class})
class SteelshaperApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to hand and searches for an Equipment")
    void returnsItselfToHandAndSearchesForEquipment() {
        addCreatureReady(player1, new SteelshaperApprentice());
        harness.addMana(player1, ManaColor.WHITE, 1);
        VulshokMorningstar morningstar = new VulshokMorningstar();
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setLibrary(player1, List.of(morningstar, goblin));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player1, "Steelshaper Apprentice");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Vulshok Morningstar");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Vulshok Morningstar");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin);
    }

    @Test
    @DisplayName("Does not find non-Equipment cards")
    void doesNotFindNonEquipmentCards() {
        addCreatureReady(player1, new SteelshaperApprentice());
        harness.addMana(player1, ManaColor.WHITE, 1);
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setLibrary(player1, List.of(goblin));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Steelshaper Apprentice");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin);
    }

    @Test
    @DisplayName("Cannot activate while it has summoning sickness")
    void cannotActivateWhileItHasSummoningSickness() {
        harness.addToBattlefield(player1, new SteelshaperApprentice());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        harness.assertOnBattlefield(player1, "Steelshaper Apprentice");
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new SteelshaperApprentice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Steelshaper Apprentice");
    }
}
