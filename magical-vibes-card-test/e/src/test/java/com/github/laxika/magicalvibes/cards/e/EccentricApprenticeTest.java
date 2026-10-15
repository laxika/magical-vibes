package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EccentricApprentice.class, HillGiantHerdgorger.class})
class EccentricApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        castEccentricApprentice(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Does not trigger before its controller completes a dungeon")
    void doesNotTriggerBeforeDungeonCompletion() {
        harness.addToBattlefield(player1, new EccentricApprentice());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Makes up to one target creature a 1/1 Bird with flying after dungeon completion")
    void makesTargetBirdAfterDungeonCompletion() {
        harness.addToBattlefield(player1, new EccentricApprentice());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, giant)).containsExactly(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Bird transformation wears off at end of turn")
    void birdTransformationWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EccentricApprentice());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, giant)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Entering advances an existing dungeon along the chosen path")
    void advancesExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        castEccentricApprentice(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Entering can choose another dungeon and resolve its first room")
    void choosesMadMage() {
        harness.setLife(player1, 20);
        castEccentricApprentice(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Its controller may choose no creature for the combat ability")
    void canDeclineCombatTarget() {
        harness.addToBattlefield(player1, new EccentricApprentice());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, giant)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Dungeon completion does not make it trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new EccentricApprentice());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's completed dungeon does not satisfy the condition")
    void opponentsDungeonDoesNotEnableAbility() {
        harness.addToBattlefield(player1, new EccentricApprentice());
        gd.playersWhoCompletedDungeon.add(player2.getId());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The Apprentice can target itself and regains its original types after cleanup")
    void canTargetItself() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new EccentricApprentice());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, apprentice.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, apprentice)).containsExactly(CardSubtype.BIRD);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, apprentice))
                .containsExactlyInAnyOrder(CardSubtype.TIEFLING, CardSubtype.WIZARD);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    private void castEccentricApprentice(Player player) {
        harness.castFromHand(player, new EccentricApprentice(), "{2}{U}");
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
