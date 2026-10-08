package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BarkhideTroll;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildSlash;
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

@CardUsed({ChannelHarm.class, AirElemental.class, Shock.class, BarkhideTroll.class})
class ChannelHarmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponent damage to you and may deal it to the chosen creature")
    void preventsOpponentDamageAndDealsItToChosenCreature() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.channelHarmShields).hasSize(1);
    }

    @Test
    @DisplayName("Prevents opponent damage to a permanent controlled by you")
    void preventsOpponentDamageToControlledPermanent() {
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player2, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from a source you control")
    void doesNotPreventOwnDamage() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player1, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Prevents combat damage from an opponent")
    void preventsOpponentCombatDamage() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void expiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new AirElemental());
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
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castShock(Player caster, java.util.UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    @Test
    @DisplayName("An illegal target on resolution prevents the entire spell from resolving")
    void doesNotPreventDamageWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new ChannelHarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, target.getId());

        castShock(player1, target.getId());
        castShock(player1, target.getId());
        harness.passBothPriorities();
        castShock(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Prevention continues after the chosen creature leaves the battlefield")
    void preventsDamageAfterTargetLeaves() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castChannelHarm(target.getId());
        castShock(player1, target.getId());
        castShock(player1, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining damage does not consume the prevention effect")
    void canDeclineEachPreventedDamageEvent() {
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not protect an opponent from your damage")
    void doesNotProtectOpponent() {
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());

        castShock(player1, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({WildSlash.class})
    @DisplayName("Damage that cannot be prevented is neither prevented nor dealt by Channel Harm")
    void doesNotPreventUnpreventableDamage() {
        Permanent target = addCreatureReady(player1, new AirElemental());
        castChannelHarm(target.getId());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        castShock(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not recheck hexproof acquired after Channel Harm resolves")
    void damagesCreatureThatGainedHexproofAfterResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BarkhideTroll()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        Permanent target = findPermanent(player2, "Barkhide Troll");
        castChannelHarm(target.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        castShock(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        // Also check after passing priority to isolate targeting from the timing defect.
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(BarkhideTroll.class::isInstance);
    }
}
