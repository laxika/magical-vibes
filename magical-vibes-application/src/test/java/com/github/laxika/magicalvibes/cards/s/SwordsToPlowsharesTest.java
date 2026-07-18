package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwordsToPlowsharesTest extends BaseCardTest {

    private void giveSwords() {
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Exiles the target creature and its controller gains life equal to its power")
    void exilesCreatureAndControllerGainsLife() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Target removed from battlefield and moved to exile (not graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));

        // Grizzly Bears has power 2 → its controller (player2) gains 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain scales with the creature's power")
    void lifeGainScalesWithPower() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Hill Giant has power 3 → its controller gains 3 life
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Life goes to the controller of the exiled creature (caster's own creature)")
    void lifeGoesToControllerOfExiledCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player2, new GrizzlyBears());
        giveSwords();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }
}
