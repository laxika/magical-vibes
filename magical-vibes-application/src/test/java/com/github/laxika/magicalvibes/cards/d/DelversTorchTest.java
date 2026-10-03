package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelversTorch.class, HillGiantHerdgorger.class})
class DelversTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Attacking with the equipped creature makes the Torch's controller choose a dungeon")
    void attackingEquippedCreatureVentureIntoDungeon() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("An unattached torch does not trigger when a creature attacks")
    void unattachedTorchDoesNotTrigger() {
        addCreatureReady(player1, new HillGiantHerdgorger());
        harness.addToBattlefield(player1, new DelversTorch());

        declareAttackers(List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Delver's Torch"));
    }

    @Test
    void equipCostsThreeAndMovesTheBoostToAnotherCreature() {
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        Permanent first = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent second = addCreatureReady(player1, new HillGiantHerdgorger());
        torch.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(torch.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
    }

    @Test
    void equipCannotBeActivatedWithOnlyTwoMana() {
        harness.addToBattlefield(player1, new DelversTorch());
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        harness.addToBattlefield(player1, new DelversTorch());
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("creature you control");
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new DelversTorch());
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
    }

    @Test
    void attackingWithAnotherCreatureDoesNotVenture() {
        Permanent equipped = addCreatureReady(player1, new HillGiantHerdgorger());
        addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(equipped.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void torchControllerVenturesEvenWhenOpponentControlsEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void attackAdvancesAnExistingDungeon() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(creature.getId());
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Goblin Lair");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
        harness.assertOnBattlefield(player1, "Goblin");
    }

    @Test
    void removingTheTorchRemovesItsBoost() {
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new DelversTorch());
        torch.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);

        gd.playerBattlefields.get(player1.getId()).remove(torch);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }
}
