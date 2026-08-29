package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArbiterOfWoeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Arbiter of Woe requires sacrificing a creature")
    void castingRequiresSacrificingCreature() {
        harness.setHand(player1, List.of(new ArbiterOfWoe()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Arbiter of Woe's enters-the-battlefield ability has its full effect")
    void entersTheBattlefieldAbility() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        castArbiter(sacrifice);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castArbiter(Permanent sacrifice) {
        harness.setHand(player1, List.of(new ArbiterOfWoe()));
        addMana();
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrifice.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
