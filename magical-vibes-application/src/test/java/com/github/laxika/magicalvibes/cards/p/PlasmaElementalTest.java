package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlasmaElemental.class, DrossCrocodile.class})
class PlasmaElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Plasma Elemental can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player1, new PlasmaElemental());
        addCreatureReady(player2, new DrossCrocodile());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Plasma Elemental deals combat damage to the defending player")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent elemental = addCreatureReady(player1, new PlasmaElemental());
        elemental.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Plasma Elemental can block an attacking creature")
    void canBlock() {
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new PlasmaElemental());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plasma Elemental does not stop another attacker from being blocked")
    void otherAttackerCanBeBlocked() {
        addCreatureReady(player1, new PlasmaElemental());
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard()).isInstanceOf(PlasmaElemental.class));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
