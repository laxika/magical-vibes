package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarulfsPackmate.class, Forest.class})
class SarulfsPackmateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(packmate));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(drawn));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A foretold Packmate cannot be cast on the same turn")
    void cannotCastOnForetellTurn() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        harness.setHand(player1, List.of(packmate));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, packmate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(packmate.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foretell requires two mana and does not draw a card")
    void foretellRequiresTwoMana() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        harness.setHand(player1, List.of(packmate));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(packmate);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting for the foretell cost still requires green mana")
    void foretellCastRequiresGreenMana() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        harness.setHand(player1, List.of(packmate));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, packmate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(packmate.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Packmate cast for its foretell cost draws on entry")
    void foretoldPackmateDrawsOnEntry() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(packmate));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, packmate.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(packmate.getId())).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sarulf's Packmate");
    }

    @Test
    @DisplayName("Packmate cannot be foretold during an opponent's turn")
    void cannotForetellDuringOpponentsTurn() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        harness.setHand(player1, List.of(packmate));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(packmate);
        assertThat(gd.findExiledCard(packmate.getId())).isNull();
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        SarulfsPackmate packmate = new SarulfsPackmate();
        harness.setHand(player1, List.of(packmate));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(packmate.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, packmate.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sarulf's Packmate");
    }
}
