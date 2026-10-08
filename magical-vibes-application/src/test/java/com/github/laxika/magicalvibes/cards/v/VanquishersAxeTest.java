package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.y.YavimayaSteelcrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanquishersAxe.class, YavimayaSteelcrusher.class})
class VanquishersAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {2} attaches Vanquisher's Axe to a creature you control")
    void equipAttachesToCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature loses the bonus when the Axe becomes unattached")
    void bonusIsLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        axe.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip pays two generic mana")
    void equipPaysTwoGenericMana() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(axe.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot equip an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addAxeReady(player1);
        Permanent creature = addCreatureReady(player2, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot equip during combat")
    void cannotEquipDuringCombat() {
        addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot equip on an opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Re-equipping transfers the bonus only on resolution")
    void reEquipTransfersBonusOnResolution() {
        Permanent axe = addAxeReady(player1);
        Permanent first = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent second = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("A departed re-equip target leaves the original attachment intact")
    void failedReEquipPreservesOriginalAttachment() {
        Permanent axe = addAxeReady(player1);
        Permanent first = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent second = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAxeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VanquishersAxe());
        perm.setSummoningSick(false);
        return perm;
    }
}
