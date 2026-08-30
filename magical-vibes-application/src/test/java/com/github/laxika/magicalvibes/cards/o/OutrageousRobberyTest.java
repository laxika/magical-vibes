package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        gs.playCardFromExile(gd, player1, spell.getId(), null, player2.getId());
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
}
