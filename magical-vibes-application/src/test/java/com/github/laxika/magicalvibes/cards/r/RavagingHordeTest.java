package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavagingHordeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RavagingHorde()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Ravaging Horde");
    }

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavagingHorde()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() ->
                harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, creatureId, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
