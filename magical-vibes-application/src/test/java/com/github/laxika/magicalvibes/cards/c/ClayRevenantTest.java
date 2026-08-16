package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClayRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped when cast")
    void entersTappedWhenCast() {
        harness.setHand(player1, List.of(new ClayRevenant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent revenant = findPermanent(player1, "Clay Revenant");
        assertThat(revenant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns it to hand")
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new ClayRevenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Clay Revenant");
        harness.assertNotInGraveyard(player1, "Clay Revenant");
    }
}
