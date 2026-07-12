package com.github.laxika.magicalvibes.cards.l;

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

class LastBreathTest extends BaseCardTest {

    private void giveLastBreath() {
        harness.setHand(player1, List.of(new LastBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Exiles a power-2 creature and its controller gains 4 life")
    void exilesCreatureAndControllerGainsLife() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Target removed from battlefield and moved to exile (not graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));

        // The exiled creature's controller (player2) gains the life, not the caster
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Life goes to the controller of the exiled creature (caster's own creature)")
    void lifeGoesToCasterWhenTargetingOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        // Provide a legal target so the spell is castable at all
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bigGuy = addCreatureReady(player2, new HillGiant());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bigGuy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }
}
