package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorchGauntlet.class, DukharaPeafowl.class})
class TorchGauntletTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Torch Gauntlet to a creature")
    void equipsCreature() {
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unattached Torch Gauntlet does not boost creatures")
    void unattachedGauntletDoesNotBoostCreature() {
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature loses Torch Gauntlet's boost when it is unattached")
    void creatureLosesBoostWhenGauntletIsUnattached() {
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gauntlet.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void reequippingMovesTheBoostOnlyOnResolution() {
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());
        Permanent first = addCreatureReady(player1, new DukharaPeafowl());
        Permanent second = addCreatureReady(player1, new DukharaPeafowl());
        gauntlet.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gauntlet.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void cannotEquipWithOnlyOneMana() {
        harness.addToBattlefield(player1, new TorchGauntlet());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new TorchGauntlet());
        Permanent creature = addCreatureReady(player2, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new TorchGauntlet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gauntlet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringUpkeep() {
        harness.addToBattlefield(player1, new TorchGauntlet());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWhileStackIsNotEmpty() {
        harness.addToBattlefield(player1, new TorchGauntlet());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
