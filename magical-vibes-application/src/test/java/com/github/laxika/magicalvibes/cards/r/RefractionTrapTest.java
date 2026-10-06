package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChainReaction;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RefractionTrap.class, GrizzlyBears.class, Shock.class, GiantGrowth.class, ChainReaction.class})
class RefractionTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage to the controller and redirects it to any target")
    void preventsDamageAndRedirectsIt() {
        Permanent attacker = addAttacker(player2);
        castForMana(player2.getId());
        harness.handlePermanentChosen(player1, attacker.getId());

        runCombatDamage();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents damage to a controlled permanent and redirects it")
    void preventsDamageToControlledPermanent() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addAttacker(player2);
        castForMana(player2.getId());
        harness.handlePermanentChosen(player1, attacker.getId());

        runCombatDamage();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can pay {W} after an opponent casts a red instant")
    void canUseAlternateCostAfterRedInstant() {
        castRedInstantFromOpponent();
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        preparePlayer1MainPhase();
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, source.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost is unavailable without an opponent red instant or sorcery")
    void alternateCostRequiresRedInstantOrSorcery() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        preparePlayer2MainPhase();
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        preparePlayer1MainPhase();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Resolution asks the controller to choose a damage source")
    void choosesSourceOnResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castForMana(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Damage from an unchosen source is not prevented")
    void onlyPreventsChosenSourceDamage() {
        Permanent chosen = addAttacker(player2);
        addAttacker(player2);
        castForMana(player2.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        runCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The controller's own red instant does not enable the alternate cost")
    void ownRedInstantDoesNotEnableAlternateCost() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Can pay the alternate cost after an opponent casts a red sorcery")
    void canUseAlternateCostAfterRedSorcery() {
        harness.setHand(player2, List.of(new ChainReaction()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        preparePlayer2MainPhase();
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        preparePlayer1MainPhase();
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Prevents only three damage when the chosen source deals five")
    void capsPreventionAtThree() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        castForMana(player2.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        runCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Prevented damage is dealt by Refraction Trap to its creature target")
    void dealsPreventedDamageToCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent attacker = addAttacker(player2);

        castForMana(target.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        runCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    private void castForMana(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castRedInstantFromOpponent() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();
        harness.castAndResolveInstant(player2, 0, player2.getId());
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = addCreatureReady(player, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player);
        return attacker;
    }

    private void runCombatDamage() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void preparePlayer1MainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
