package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfStromgald;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HaakonStromgaldScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be cast from hand")
    void cannotBeCastFromHand() {
        harness.setHand(player1, List.of(new HaakonStromgaldScourge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from hand");
    }

    @Test
    @DisplayName("Can be cast from graveyard")
    void canBeCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(new HaakonStromgaldScourge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Haakon, Stromgald Scourge");
    }

    @Test
    @DisplayName("Allows Knight spells to be cast from the controller's graveyard")
    void allowsKnightSpellsFromGraveyard() {
        harness.addToBattlefield(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player1, List.of(new KnightOfStromgald()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knight of Stromgald");
    }

    @Test
    @DisplayName("Does not allow non-Knight spells to be cast from the graveyard")
    void doesNotAllowNonKnightSpellsFromGraveyard() {
        harness.addToBattlefield(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Controller loses 2 life when Haakon dies")
    void controllerLosesLifeWhenHaakonDies() {
        Permanent haakon = harness.addToBattlefieldAndReturn(player1, new HaakonStromgaldScourge());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, haakon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Haakon, Stromgald Scourge");
    }
}
