package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantsSkewer.class, AirElemental.class, GrizzlyBears.class, ProdigalPyromancer.class,
        SuntailHawk.class})
class GiantsSkewerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent skewer = addSkewerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        skewer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage to a creature creates a Food token")
    void combatDamageToCreatureCreatesFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        skewer.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Combat damage to a player does not create a Food token")
    void combatDamageToPlayerDoesNotCreateFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        skewer.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to a creature does not create a Food token")
    void noncombatDamageToCreatureDoesNotCreateFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        skewer.setAttachedTo(pyromancer.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private Permanent addSkewerReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GiantsSkewer());
    }

    @Test
    void equipAttachesAndMovesTheBoost() {
        Permanent skewer = addSkewerReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        skewer.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(skewer.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addSkewerReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        addSkewerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equippedBlockerCreatesFood() {
        Permanent skewer = addSkewerReady(player2);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        skewer.setAttachedTo(blocker.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Food")).isOne();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void unequippedCreatureDoesNotCreateFood() {
        addSkewerReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void opponentControlledEquippedCreatureCreatesFoodForEquipmentController() {
        Permanent skewer = addSkewerReady(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        skewer.setAttachedTo(attacker.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player2, "Food")).isZero();
    }
}
