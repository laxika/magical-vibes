package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingPowder.class, Frostling.class, GnarledMass.class})
class BlindingPowderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Blinding Powder to target creature")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent powder = addPowderReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(powder.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unattached Blinding Powder grants no ability")
    void unattachedPowderGrantsNoAbility() {
        addCreatureReady(player1, new GnarledMass());
        addPowderReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Unattaching Blinding Powder prevents combat damage to the equipped creature this turn")
    void unattachPreventsCombatDamageToEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(powder.getAttachedTo()).isNull();

        Permanent attacker = addCreatureReady(player2, new GnarledMass());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Blinding Powder does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new Frostling());
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent damageSource = addCreatureReady(player1, new Frostling());
        int damageSourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(damageSource);
        harness.activateAbility(player1, damageSourceIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can unattach Powder as an immediate cost")
    void unattachIsImmediateAndRequiresNoTap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        creature.setSummoningSick(true);
        creature.tap();
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(powder.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(powder);
        assertThat(gd.stack).hasSize(1);
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Re-equipping Powder leaves prevention on the original creature only")
    void reequippingDoesNotMovePrevention() {
        Permanent original = addCreatureReady(player1, new GnarledMass());
        Permanent replacement = addCreatureReady(player1, new GnarledMass());
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(original.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, replacement.getId());
        harness.passBothPriorities();
        assertThat(powder.getAttachedTo()).isEqualTo(replacement.getId());

        Permanent firstAttacker = addCreatureReady(player2, new GnarledMass());
        Permanent secondAttacker = addCreatureReady(player2, new GnarledMass());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original).doesNotContain(replacement);
        assertThat(original.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstAttacker, secondAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(powder);
        assertThat(powder.getAttachedTo()).isNull();
    }

    private Permanent addPowderReady(Player player) {
        return addCreatureReady(player, new BlindingPowder());
    }
}
