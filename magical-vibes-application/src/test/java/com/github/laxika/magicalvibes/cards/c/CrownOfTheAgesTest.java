package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrownOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating puts the ability on the stack with both targets")
    void activatingAbilityPutsOnStack() {
        Permanent crown = addReadyCrown(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(aura.getId(), creature2.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetIds()).containsExactly(aura.getId(), creature2.getId());
        assertThat(crown.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving moves the Aura from one creature to another")
    void resolvingMovesAura() {
        addReadyCrown(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(aura.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability fizzles if the Aura leaves the battlefield before resolution")
    void fizzlesIfAuraLeaves() {
        addReadyCrown(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(aura.getId(), creature2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(aura.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Ability fizzles if the destination creature leaves before resolution")
    void fizzlesIfCreatureLeaves() {
        addReadyCrown(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(aura.getId(), creature2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(aura.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Cannot target a non-Aura permanent as the Aura to move")
    void cannotTargetNonAura() {
        addReadyCrown(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature1.getId(), creature2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyCrown(Player player) {
        Permanent perm = new Permanent(new CrownOfTheAges());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
