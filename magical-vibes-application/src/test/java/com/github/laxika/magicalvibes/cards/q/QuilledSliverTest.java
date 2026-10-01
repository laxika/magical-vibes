package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuilledSliver.class, BonesplitterSliver.class, AshcoatBear.class})
class QuilledSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Quilled Sliver deals 1 damage to an attacking creature")
    void damagesAttackingCreature() {
        Permanent quilledSliver = addCreatureReady(player1, new QuilledSliver());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(quilledSliver.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("All Slivers gain the ability and can damage a blocking creature")
    void grantsAbilityToAllSlivers() {
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        Permanent bonesplitterSliver = addCreatureReady(player2, new BonesplitterSliver());
        addCreatureReady(player1, new QuilledSliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(bonesplitterSliver), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(bonesplitterSliver.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain Quilled Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent nonSliver = addCreatureReady(player1, new AshcoatBear());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void rejectsNonCombatCreature() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }
}
