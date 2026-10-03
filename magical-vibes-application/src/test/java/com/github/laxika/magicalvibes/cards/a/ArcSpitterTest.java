package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
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

@CardUsed({ArcSpitter.class, SnoopingNewsie.class})
class ArcSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Arc Spitter to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent spitter = addArcSpitterReady(player1);
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spitter.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can deal 1 damage to a creature blocking it")
    void equippedCreatureDamagesBlocker() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());

        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Arc Spitter cannot target a creature that is not blocking the equipped creature")
    void cannotTargetNonBlocker() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        Permanent bystander = addCreatureReady(player2, new SnoopingNewsie());

        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unattached Arc Spitter grants no activated ability")
    void unattachedSpitterGrantsNoAbility() {
        addCreatureReady(player1, new SnoopingNewsie());
        addArcSpitterReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void cannotEquipOpponentsCreature() {
        addArcSpitterReady(player1);
        Permanent opponent = addCreatureReady(player2, new SnoopingNewsie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        addArcSpitterReady(player1);
        addCreatureReady(player2, new SnoopingNewsie());
        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureBlockingAnotherAttacker() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void activatedAbilityStillResolvesAfterEquipmentMoves() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent otherCreature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());

        spitter.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void abilityDoesNotResolveIfTargetStopsBlocking() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());

        blocker.clearCombatState();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void damageAbilityRequiresMana() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        blockCreature();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void tappedAttackerCanActivateRepeatedlyToKillBlocker() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new SnoopingNewsie());
        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    private Permanent addArcSpitterReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArcSpitter());
    }

    private void blockCreature() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
