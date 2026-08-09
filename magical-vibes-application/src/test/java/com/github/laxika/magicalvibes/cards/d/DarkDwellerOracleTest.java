package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarkDwellerOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and exiles the top card with end-of-turn play permission")
    void sacrificesCreatureAndExilesTopCard() {
        addOracleAndFodder();
        Card top = putSpellOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Can sacrifice itself to activate")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new DarkDwellerOracle());
        Card top = putSpellOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark-Dweller Oracle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Exiles nothing when the library is empty")
    void exilesNothingWithEmptyLibrary() {
        addOracleAndFodder();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    private void addOracleAndFodder() {
        harness.addToBattlefield(player1, new DarkDwellerOracle());
        harness.addToBattlefield(player1, new GrizzlyBears());
    }

    private Card putSpellOnTop(Player player) {
        Card card = new Card();
        card.setName("Exiled Spell");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }
}
