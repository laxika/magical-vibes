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

        Permanent attacker = new Permanent(new GnarledMass());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

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

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent damageSource = addCreatureReady(player1, new Frostling());
        int damageSourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(damageSource);
        harness.activateAbility(player1, damageSourceIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addPowderReady(Player player) {
        Permanent powder = new Permanent(new BlindingPowder());
        powder.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(powder);
        return powder;
    }
}
