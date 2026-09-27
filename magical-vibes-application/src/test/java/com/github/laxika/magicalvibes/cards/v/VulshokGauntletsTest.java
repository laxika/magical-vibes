package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokGauntlets.class, Frogmite.class})
class VulshokGauntletsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unattached Vulshok Gauntlets do not boost creatures")
    void unattachedGauntletsDoNotBoost() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        addGauntletsReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature does not untap during its controller's untap step")
    void equippedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        creature.tap();
        Permanent free = addCreatureReady(player1, new Frogmite());
        free.tap();

        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature untaps after Vulshok Gauntlets is removed")
    void equippedCreatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        creature.tap();
        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gauntlets);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equip attaches Vulshok Gauntlets to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent gauntlets = addGauntletsReady(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlets.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip requires three generic mana")
    void equipRequiresThreeGenericMana() {
        Permanent gauntlets = addGauntletsReady(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gauntlets.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can target only a creature controlled by its controller")
    void equipCannotTargetOpponentCreature() {
        Permanent gauntlets = addGauntletsReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gauntlets.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can be activated only at sorcery speed")
    void equipCannotBeActivatedOutsideSorcerySpeed() {
        Permanent gauntlets = addGauntletsReady(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gauntlets.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip does nothing if the target leaves before resolution")
    void equipFizzlesIfTargetLeavesBeforeResolution() {
        Permanent gauntlets = addGauntletsReady(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gauntlets.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGauntletsReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VulshokGauntlets());
        perm.setSummoningSick(false);
        return perm;
    }
}
