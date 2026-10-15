package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SirenSongLyre.class, NyxbornRollicker.class})
class SirenSongLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can tap to tap target creature")
    void equippedCreatureTapsTargetCreature() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent targetCreature = addCreatureReady(player2, new NyxbornRollicker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Siren Song Lyre can equip a creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lyre.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Siren Song Lyre cannot target a player")
    void grantedAbilityRequiresCreatureTarget() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Siren Song Lyre removes the granted ability")
    void removingLyreRemovesGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent targetCreature = addCreatureReady(player2, new NyxbornRollicker());
        gd.playerBattlefields.get(player1.getId()).remove(lyre);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornRollicker());
        creature.setSummoningSick(true);
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        creature.tap();
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void creatureCanTargetItselfAndTappedLyreDoesNotPreventActivation() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        lyre.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void alreadyTappedCreatureIsALegalTarget() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(lyre.isTapped()).isFalse();
    }

    @Test
    void removingLyreAfterActivationDoesNotStopResolution() {
        Permanent creature = addCreatureReady(player1, new NyxbornRollicker());
        Permanent lyre = addCreatureReady(player1, new SirenSongLyre());
        lyre.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(creature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(lyre);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addCreatureReady(player1, new SirenSongLyre());
        Permanent target = addCreatureReady(player2, new NyxbornRollicker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
