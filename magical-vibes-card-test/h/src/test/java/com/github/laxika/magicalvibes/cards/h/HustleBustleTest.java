package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HustleBustle.class, GrizzlyBears.class})
class HustleBustleTest extends BaseCardTest {

    @Test
    @DisplayName("Hustle requires its target to attack or block")
    void hustleRequiresTargetToAttackOrBlock() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Bustle boosts your creatures and grants them trample")
    void bustleBoostsOwnCreaturesAndGrantsTrample() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameQueryService().getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(harness.getGameQueryService().hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bustle may turn a face-down creature face up")
    void bustleMayTurnFaceDownCreatureFaceUp() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Bustle lets its controller choose which face-down creature to turn up")
    void bustleChoosesFaceDownCreatureToTurnFaceUp() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setFaceDownAsCloaked();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isFaceDown()).isTrue();
        assertThat(second.isFaceDown()).isFalse();
    }

    @Test
    void bustleCannotBeCastDuringUpkeep() {
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bustleCannotBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bustleIsASorcerySpellOnTheStack() {
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    void hustleCanBeCastDuringUpkeepAndRequiresOpposingCreatureToBlock() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void bustleCanDeclineTurningAFaceDownCreatureUp() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(faceDown.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, faceDown, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void bustlePreservesBoostAndTrampleWhenTurningCreatureFaceUp() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        Permanent opposingFaceDown = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingFaceDown.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, faceDown, Keyword.TRAMPLE)).isTrue();
        assertThat(opposingFaceDown.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingFaceDown)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingFaceDown, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void bustleDoesNotBoostCreaturesEnteringAfterResolution() {
        Permanent existing = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, existing, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void bustleLeavesACloakedInstantOrSorceryFaceDown() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new HustleBustle());
        faceDown.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDown);
        assertThat(gqs.getEffectivePower(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, faceDown)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, faceDown, Keyword.TRAMPLE)).isTrue();
    }
}
