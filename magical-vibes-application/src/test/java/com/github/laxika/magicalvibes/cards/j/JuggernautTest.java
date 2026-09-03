package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Juggernaut.class, WallOfSwords.class, GrizzlyBears.class})
class JuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Juggernaut puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new Juggernaut(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Juggernaut");
    }

    @Test
    @DisplayName("Juggernaut must attack each combat if able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new Juggernaut());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void doesNotHaveToAttackWhenTapped() {
        Permanent juggernaut = addCreatureReady(player1, new Juggernaut());
        juggernaut.tap();

        declareAttackers(List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Juggernaut deals 5 combat damage when unblocked")
    void dealsFiveDamageUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new Juggernaut());
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Juggernaut cannot be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent juggernaut = addCreatureReady(player1, new Juggernaut());
        juggernaut.setAttacking(true);

        addCreatureReady(player2, new WallOfSwords());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by non-Wall creatures");
    }

    @Test
    @DisplayName("Juggernaut can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent juggernaut = addCreatureReady(player1, new Juggernaut());
        juggernaut.setAttacking(true);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }
}

