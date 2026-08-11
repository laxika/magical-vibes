package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecoupTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants flashback to target sorcery in graveyard")
    void grantsFlashbackToTargetSorcery() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        harness.castSorcery(player1, 0, divination.getId());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(divination.getId());
    }

    @Test
    @DisplayName("Cannot target an instant or creature card in graveyard")
    void cannotTargetNonSorceryCard() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flashback uses the sorcery's mana cost and exiles it after casting")
    void grantedFlashbackAllowsCastingSorcery() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, divination.getId());
        harness.passBothPriorities();

        harness.castFlashback(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Divination");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Divination"));
    }

    @Test
    @DisplayName("Recoup can be cast from the graveyard for its flashback cost")
    void recoupHasFlashback() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(new Recoup(), divination));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, divination.getId());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(divination.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Recoup"));
    }

    private void addRecoupMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
