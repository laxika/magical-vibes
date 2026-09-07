package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldOut.class, GrizzlyBears.class})
class SoldOutTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and creates a Clue if it was dealt damage this turn")
    void exilesDamagedCreatureAndCreatesClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castSoldOut(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature without creating a Clue if it was not dealt damage this turn")
    void exilesUndamagedCreatureWithoutCreatingClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSoldOut(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    private void castSoldOut(Permanent target) {
        harness.setHand(player1, List.of(new SoldOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
