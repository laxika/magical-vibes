package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Goremand")
class GoremandTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a creature as an additional cost")
    void castingSacrificesCreature() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("ETB makes each opponent sacrifice a creature")
    void etbMakesEachOpponentSacrifice() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goremand");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Goremand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
