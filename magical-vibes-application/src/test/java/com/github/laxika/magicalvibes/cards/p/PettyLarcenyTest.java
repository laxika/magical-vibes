package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PettyLarceny.class, Island.class, Shock.class})
class PettyLarcenyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards face down, creates a Treasure, and grants persistent play permission")
    void exilesTopTwoCardsAndCreatesTreasure() {
        Card first = new Shock();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player2, List.of(first, second, third));
        castPettyLarceny();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Casts a spell from exile using mana of any type")
    void castsExiledSpellUsingAnyManaType() {
        Shock spell = new Shock();
        harness.setLibrary(player2, List.of(spell));
        castPettyLarceny();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }

    @Test
    @DisplayName("Freerunning casts for {1}{B} after qualifying combat damage")
    void freerunningCastsForAlternateCost() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new PettyLarceny()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target the controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new PettyLarceny()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castPettyLarceny() {
        harness.setHand(player1, List.of(new PettyLarceny()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
