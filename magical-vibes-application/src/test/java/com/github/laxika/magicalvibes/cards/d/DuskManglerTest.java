package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskMangler.class, GrizzlyBears.class, LlanowarElves.class})
class DuskManglerTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuskMangler()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void discardsCardAsAdditionalCost() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DuskMangler(), new GrizzlyBears()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void paysLifeAsAdditionalCost() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        harness.castCreature(player1, 0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void cannotCastWithoutAnyAdditionalCostPayment() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        harness.assertInHand(player1, "Dusk Mangler");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
