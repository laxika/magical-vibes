package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuilledSliver.class, BonesplitterSliver.class, AshcoatBear.class,
        ArtificialEvolution.class, Bitterblossom.class})
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
        addCreatureReady(player1, new AshcoatBear());
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

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new QuilledSliver());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void cannotActivateAgainWithoutUntapping() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void activatedGrantedAbilityResolvesAfterQuilledSliverLeaves() {
        Permanent quilledSliver = addCreatureReady(player1, new QuilledSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.activateAbility(player1, 1, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(quilledSliver);
        harness.passBothPriorities();

        assertThat(otherSliver.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).isEmpty();
    }

    @Test
    void abilityDoesNotDamageTargetThatHasLeftCombat() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.activateAbility(player1, 0, null, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void grantsAbilityToNoncreatureSliverPermanents() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent bitterblossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, bitterblossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.activateAbility(player1, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(bitterblossom.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }
}
