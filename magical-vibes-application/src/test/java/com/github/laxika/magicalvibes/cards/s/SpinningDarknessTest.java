package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpinningDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the targeted nonblack creature and the caster gains 3 life")
    void damagesNonblackCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A black creature is not a legal target")
    void blackCreatureIsIllegalTarget() {
        harness.addToBattlefield(player2, new BogImp());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Bog Imp");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling the top three black cards of the graveyard casts it without paying mana")
    void alternateCostExilesTopThreeBlackCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castWithAlternateCost(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Spinning Darkness");
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    @DisplayName("Only the topmost black cards are exiled; nonblack cards stay in the graveyard")
    void alternateCostSkipsNonblackCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(
                new DarkRitual(), new GrizzlyBears(), new DarkRitual(), new DarkRitual()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castWithAlternateCost(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears", "Spinning Darkness");
    }

    @Test
    @DisplayName("The alternate cost can't be paid with fewer than three black cards in the graveyard")
    void alternateCostRequiresThreeBlackCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(new DarkRitual(), new DarkRitual()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
