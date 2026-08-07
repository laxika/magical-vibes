package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KithkinArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThree() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachArmor(attacker);

        Permanent blocker = addReadyStats(player2, 3, 3);

        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a creature with power 2")
    void canBeBlockedByPowerTwo() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachArmor(attacker);

        Permanent blocker = addReadyStats(player2, 2, 2);

        beginDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura prevents the chosen source's next damage to the enchanted creature")
    void preventsNextDamageFromChosenSource() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = attachArmor(bears);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, armor), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.assertInGraveyard(player1, "Kithkin Armor");
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("The shield only covers the enchanted creature, not other permanents")
    void shieldDoesNotCoverOtherCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = attachArmor(bears);
        Permanent other = addReadyStats(player1, 3, 3);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, armor), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, other.getId());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
    }

    @Test
    @DisplayName("Only the next damage event from the chosen source is prevented")
    void onlyTheNextDamageEventIsPrevented() {
        Permanent bears = addReadyStats(player1, 4, 4);
        Permanent armor = attachArmor(bears);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, armor), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getMarkedDamage()).isZero();

        pyromancer.untap();
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void otherSourceIsNotPrevented() {
        Permanent bears = addReadyStats(player1, 4, 4);
        Permanent armor = attachArmor(bears);
        Permanent chosen = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent otherPyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, armor), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.activateAbility(player2, indexOf(player2, otherPyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private Permanent attachArmor(Permanent enchanted) {
        Permanent aura = new Permanent(new KithkinArmor());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
