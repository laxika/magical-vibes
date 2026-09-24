package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.IronfistCrusher;
import com.github.laxika.magicalvibes.cards.n.NovaCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiseryCharm.class, NovaCleric.class, IronfistCrusher.class})
class MiseryCharmTest extends BaseCardTest {

    @Test
    void destroysTargetCleric() {
        harness.addToBattlefield(player2, new NovaCleric());
        castCharm(0, harness.getPermanentId(player2, "Nova Cleric"));

        harness.assertInGraveyard(player2, "Nova Cleric");
    }

    @Test
    void cannotDestroyNonCleric() {
        harness.addToBattlefield(player2, new IronfistCrusher());

        assertThatThrownBy(() -> castCharm(
                0, harness.getPermanentId(player2, "Ironfist Crusher")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTargetClericFromGraveyard() {
        Card cleric = new NovaCleric();
        harness.setGraveyard(player1, List.of(cleric));
        castCharm(1, cleric.getId());

        harness.assertInHand(player1, "Nova Cleric");
        harness.assertNotInGraveyard(player1, "Nova Cleric");
    }

    @Test
    void cannotReturnNonClericFromGraveyard() {
        Card nonCleric = new IronfistCrusher();
        harness.setGraveyard(player1, List.of(nonCleric));

        assertThatThrownBy(() -> castCharm(1, nonCleric.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnClericFromOpponentsGraveyard() {
        Card cleric = new NovaCleric();
        harness.setGraveyard(player2, List.of(cleric));

        assertThatThrownBy(() -> castCharm(1, cleric.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void makesTargetPlayerLoseTwoLife() {
        harness.setLife(player2, 20);

        castCharm(2, player2.getId());

        harness.assertLife(player2, 18);
    }

    private void castCharm(int modeIndex, UUID targetId) {
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, modeIndex, targetId);
        harness.passBothPriorities();
    }
}
