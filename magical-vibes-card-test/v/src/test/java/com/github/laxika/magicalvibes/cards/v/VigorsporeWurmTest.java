package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DevkarinDissident;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigorsporeWurm.class, DevkarinDissident.class, DirectCurrent.class})
class VigorsporeWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +X/+X and vigilance for creature cards in your graveyard")
    void etbBoostsTargetByOwnCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new DevkarinDissident(), new DevkarinDissident(), new DirectCurrent()));
        harness.setGraveyard(player2, List.of(new DevkarinDissident()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());

        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("ETB boost and vigilance wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        harness.setGraveyard(player1, List.of(new DevkarinDissident()));

        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Vigorspore Wurm cannot be blocked by more than one creature")
    void cannotBeBlockedByMoreThanOneCreature() {
        Permanent blockerOne = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        Permanent blockerTwo = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new VigorsporeWurm());
        wurm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerOneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne);
        int blockerTwoIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo);
        int wurmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wurm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerOneIndex, wurmIndex),
                new BlockerAssignment(blockerTwoIndex, wurmIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty creature graveyard still grants vigilance")
    void grantsVigilanceWithNoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new DirectCurrent()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Undergrowth counts at resolution and does not track later graveyard changes")
    void countsCreatureCardsOnlyWhenTriggerResolves() {
        harness.setGraveyard(player1, List.of(new DevkarinDissident()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new DevkarinDissident(), new DevkarinDissident()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.VIGILANCE);

        harness.setGraveyard(player1, List.of());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Vigorspore Wurm can be blocked by exactly one creature")
    void canBeBlockedByOneCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new VigorsporeWurm());
        wurm.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int wurmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wurm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, wurmIndex)));

        harness.resolveCombatDamage();

        harness.assertInGraveyard(player2, "Devkarin Dissident");
        harness.assertOnBattlefield(player1, "Vigorspore Wurm");
        harness.assertLife(player2, 20);
    }
}
