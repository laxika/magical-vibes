package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmguardFamiliar.class, GrizzlyBears.class, Shock.class})
class ArmguardFamiliarTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndWard() {
        Permanent armguard = addReadyArmguard(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        armguard.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheArmguard() {
        Permanent armguard = addReadyArmguard(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armguard.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, armguard)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(armguard.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, armguard)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void wardCountersAnUnpaidOpponentSpell() {
        Permanent armguard = addReadyArmguard(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        armguard.setAttachedTo(creature.getId());

        castShockAtCreature(creature, 1);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void payingWardLetsOpponentSpellResolve() {
        Permanent armguard = addReadyArmguard(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        armguard.setAttachedTo(creature.getId());

        castShockAtCreature(creature, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent armguard = addReadyArmguard(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armguard.getAttachedTo()).isNull();
    }

    private void castShockAtCreature(Permanent creature, int mana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, mana - 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyArmguard(Player player) {
        Permanent armguard = new Permanent(new ArmguardFamiliar());
        armguard.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(armguard);
        return armguard;
    }
}
