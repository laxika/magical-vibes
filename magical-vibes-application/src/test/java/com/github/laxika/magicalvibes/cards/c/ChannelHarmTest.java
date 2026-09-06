package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelHarmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponent damage to you and may deal it to the chosen creature")
    void preventsOpponentDamageAndDealsItToChosenCreature() {
        harness.setLife(player1, 20);
        Permanent target = addReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.channelHarmShields).hasSize(1);
    }

    @Test
    @DisplayName("Prevents opponent damage to a permanent controlled by you")
    void preventsOpponentDamageToControlledPermanent() {
        Permanent target = addReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from a source you control")
    void doesNotPreventOwnDamage() {
        harness.setLife(player1, 20);
        Permanent target = addReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player1, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Prevents combat damage from an opponent")
    void preventsOpponentCombatDamage() {
        harness.setLife(player1, 20);
        Permanent target = addReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        Permanent attacker = addReady(player2, new AirElemental());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void expiresAtEndOfTurn() {
        Permanent target = addReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.channelHarmShields).isEmpty();
    }

    private void castChannelHarm(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChannelHarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void castShock(Player caster, java.util.UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
