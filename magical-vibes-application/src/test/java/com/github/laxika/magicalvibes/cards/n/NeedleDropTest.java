package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeedleDropTest extends BaseCardTest {

    // ===== Damage a creature dealt damage this turn + draw =====

    @Test
    @DisplayName("Deals 1 damage to a creature that was dealt damage this turn and draws a card")
    void dealsDamageToDamagedCreatureAndDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Damage a player dealt damage this turn + draw =====

    @Test
    @DisplayName("Deals 1 damage to a player that was dealt damage this turn and draws a card")
    void dealsDamageToDamagedPlayerAndDraws() {
        harness.setLife(player2, 20);
        gd.playersDealtDamageThisTurn.add(player2.getId());

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        // A damaged Hill Giant provides a legal target so the spell is castable (CR 601.2c);
        // the Grizzly Bears we actually target was not dealt damage and must be rejected.
        harness.addToBattlefield(player2, new HillGiant());
        gd.permanentsDealtDamageThisTurn.add(harness.getPermanentId(player2, "Hill Giant"));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID undamagedTargetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, undamagedTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a player that was not dealt damage this turn")
    void cannotTargetUndamagedPlayer() {
        // A damaged Hill Giant makes the spell castable; player2 took no damage and is illegal.
        harness.addToBattlefield(player2, new HillGiant());
        gd.permanentsDealtDamageThisTurn.add(harness.getPermanentId(player2, "Hill Giant"));

        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }
}
