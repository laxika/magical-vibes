package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TormentorsTridentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the equip ability attaches the Trident to the target creature")
    void equipAttachesToCreature() {
        Permanent trident = addTridentReady();
        Permanent bears = addBearsReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(trident.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addBearsReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creatures are unaffected")
    void otherCreaturesUnaffected() {
        Permanent bears = addBearsReady();
        Permanent other = addBearsReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature must attack each combat if able")
    void equippedCreatureMustAttack() {
        Permanent bears = addBearsReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(bears.getId());

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A creature the Trident is not attached to is not forced to attack")
    void unequippedCreatureIsNotForcedToAttack() {
        Permanent bears = addBearsReady();
        addTridentReady();

        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature is not forced to attack while tapped")
    void tappedCreatureIsNotForcedToAttack() {
        Permanent bears = addBearsReady();
        bears.tap();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(bears.getId());

        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Equip costs {3}")
    void equipCostsThree() {
        addTridentReady();
        Permanent bears = addBearsReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addTridentReady() {
        Permanent perm = new Permanent(new TormentorsTrident());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addBearsReady() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
