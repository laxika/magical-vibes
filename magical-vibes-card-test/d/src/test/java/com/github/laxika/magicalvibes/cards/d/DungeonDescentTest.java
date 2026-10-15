package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bulette;
import com.github.laxika.magicalvibes.cards.t.TargNar;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DungeonDescent.class, TargNar.class, Bulette.class})
class DungeonDescentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new DungeonDescent()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Dungeon Descent").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new DungeonDescent());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sorcery ability taps a legendary creature and ventures into the dungeon")
    void tapsLegendaryCreatureAndVentures() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(descent.isTapped()).isTrue();
        assertThat(legendaryCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Cannot pay the venture ability by tapping a nonlegendary creature")
    void requiresLegendaryCreature() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent nonlegendaryCreature = harness.addToBattlefieldAndReturn(player1, new Bulette());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(nonlegendaryCreature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void canTapSummoningSickLegendaryCreatureAndChooseAnotherDungeon() {
        harness.addToBattlefield(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        creature.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, 21);
    }

    @Test
    void advancesExistingDungeonAfterLegendaryCreatureLeaves() {
        harness.addToBattlefield(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    void cannotTapAlreadyTappedLegendaryCreature() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapOpponentsLegendaryCreature() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TargNar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void cannotActivateDuringCombatOrOpponentsTurn() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void cannotActivateWithoutFourMana() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedDungeonDescent() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        descent.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DungeonDescent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TargNar());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(descent.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }
}
