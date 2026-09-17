package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladebackSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class BladebackSliverTest extends BaseCardTest {

    @Test
    void hellbentGrantsDamageAbilityToSliversYouControl() {
        Permanent bladeback = addCreatureReady(player1, new BladebackSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, indexOf(bladeback), null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(sliver), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void damageAbilityIsUnavailableWithCardsInHand() {
        addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityIsGrantedOnlyToSliversYouControl() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponentSliver), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityCannotTargetACreature() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
