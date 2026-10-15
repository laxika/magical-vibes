package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutrageousRobbery.class, Island.class, Shock.class})
class OutrageousRobberyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X cards face down and lets the controller play them indefinitely")
    void exilesTopCardsFaceDownWithPersistentPlayPermission() {
        Shock spell = new Shock();
        Island land = new Island();
        harness.setLibrary(player2, List.of(spell, land));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(spell.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(spell.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Can cast an exiled spell using mana of any type")
    void castsExiledSpellWithManaOfAnyType() {
        Shock spell = new Shock();
        harness.setLibrary(player2, List.of(spell));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }

    @Test
    @DisplayName("Cannot target the controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("X equal to zero leaves the opponent's library unchanged")
    void zeroExilesNothing() {
        Shock spell = new Shock();
        harness.setLibrary(player2, List.of(spell));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("X larger than the library exiles every remaining card")
    void exilesOnlyAvailableCards() {
        Shock spell = new Shock();
        Island land = new Island();
        harness.setLibrary(player2, List.of(spell, land));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell, land);
        assertThat(gd.findExiledCard(spell.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(land.getId()).faceDown()).isTrue();
    }

    @Test
    @DisplayName("Can play an exiled land, but it consumes the normal land play")
    void playsExiledLandWithNormalLandLimit() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("The opponent cannot cast a card merely because they own it")
    void ownerDoesNotReceivePlayPermission() {
        Shock spell = new Shock();
        harness.setLibrary(player2, List.of(spell));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can look at a face-down exiled card even without mana to cast it")
    void canLookAtExiledCardWithoutMana() {
        Shock spell = new Shock();
        harness.setLibrary(player2, List.of(spell));
        harness.setHand(player1, List.of(new OutrageousRobbery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.publishState();

        GameStateMessage controllerState = new JacksonConfig().objectMapper().readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = new JacksonConfig().objectMapper().readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);

        assertThat(opponentState.lookedAtExileCards()).noneMatch(card -> card.id().equals(spell.getId()));
        assertThat(controllerState.lookedAtExileCards()).anyMatch(card -> card.id().equals(spell.getId()));
    }
}
