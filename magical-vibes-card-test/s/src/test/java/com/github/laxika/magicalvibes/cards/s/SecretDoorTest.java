package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecretDoor.class})
class SecretDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Secret Door makes its controller venture into the dungeon")
    void activatesToVentureIntoDungeon() {
        Permanent secretDoor = addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(secretDoor.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Secret Door's ability cannot be activated outside its controller's main phase")
    void requiresSorceryTiming() {
        addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWhileAnotherActivationIsOnStack() {
        addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void canActivateWhileTappedAndSummoningSickInPostcombatMainPhase() {
        Permanent secretDoor = harness.addToBattlefieldAndReturn(player1, new SecretDoor());
        secretDoor.setSummoningSick(true);
        secretDoor.tap();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(secretDoor.isTapped()).isTrue();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void advancesExistingDungeonAlongChosenBranch() {
        addCreatureReady(player1, new SecretDoor());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Lair");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void cannotPayActivationWithOnlyColorlessMana() {
        addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void cannotActivateWithLessThanFiveMana() {
        addCreatureReady(player1, new SecretDoor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new SecretDoor());

        assertThatThrownBy(() -> declareAttackers(java.util.List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
