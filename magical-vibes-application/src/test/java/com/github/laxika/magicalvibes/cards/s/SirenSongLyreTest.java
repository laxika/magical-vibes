package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SirenSongLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can tap to tap target creature")
    void equippedCreatureTapsTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lyre = addLyreReady(player1);
        lyre.setAttachedTo(creature.getId());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Siren Song Lyre can equip a creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lyre = addLyreReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lyre.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Siren Song Lyre cannot target a player")
    void grantedAbilityRequiresCreatureTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lyre = addLyreReady(player1);
        lyre.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Siren Song Lyre removes the granted ability")
    void removingLyreRemovesGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lyre = addLyreReady(player1);
        lyre.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lyre);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLyreReady(Player player) {
        Permanent permanent = new Permanent(new SirenSongLyre());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player, GrizzlyBears creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
