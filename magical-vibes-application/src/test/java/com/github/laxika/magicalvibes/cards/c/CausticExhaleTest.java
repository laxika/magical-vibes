package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CausticExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature -3/-3 when a Dragon is beheld from the battlefield")
    void beheldDragonPermanentGivesMinusThreeMinusThree() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"),
                List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gives a target creature -3/-3 when a Dragon is beheld from hand")
    void beheldDragonCardGivesMinusThreeMinusThree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CausticExhale(), new DragonWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"),
                List.of(), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can pay {1} instead of beholding a Dragon")
    void paysAdditionalManaWithoutDragon() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast for only {B} without a Dragon")
    void requiresDragonOrAdditionalMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
