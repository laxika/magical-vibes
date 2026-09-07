package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorosFuryShield.class, GrizzlyBears.class})
class BorosFuryShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the target attacker only")
    void preventsCombatDamageFromTargetAttackerOnly() {
        harness.setLife(player1, 20);
        Permanent target = addAttacker(player2, 2, 2);
        addAttacker(player2, 2, 2);

        castShield(player2, target, ManaColor.WHITE, 2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Red spent deals damage equal to the target's power to its controller")
    void redSpentDealsTargetPowerDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addAttacker(player2, 3, 3);

        castShield(player2, target, ManaColor.RED, 2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target a blocking creature and prevent its combat damage")
    void preventsCombatDamageFromTargetBlocker() {
        Permanent attacker = addAttacker(player1, 3, 3);
        Permanent blocker = addBlocker(player2, 2, 2, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BorosFuryShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosFuryShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShield(Player attackingPlayer, Permanent target, ManaColor coloredMana, int genericMana) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BorosFuryShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, coloredMana, genericMana);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, int power, int toughness) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int attackerIndex) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setBlocking(true);
        permanent.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }
}
