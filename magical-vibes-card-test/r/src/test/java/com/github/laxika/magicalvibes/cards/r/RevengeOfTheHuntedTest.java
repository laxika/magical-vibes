package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevengeOfTheHunted.class, GrizzlyBears.class, MoorlandInquisitor.class})
class RevengeOfTheHuntedTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +6/+6, trample and the lure flag")
    void pumpsGrantsTrampleAndLure() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(bears.getEffectiveToughness()).isEqualTo(8);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Everything wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    void allUntappedCreaturesMustBlockButTappedCreaturesNeedNot() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        tapped.tap();
        harness.setHand(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveSorcery(player1, 0, attacker.getId());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(second.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    void miracleCastsDuringDrawStepForOneGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setLibrary(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Revenge of the Hunted");
    }

    @Test
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new RevengeOfTheHunted()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Revenge of the Hunted");
    }

    @Test
    void decliningMiracleRevealLeavesCardInHand() {
        harness.setLibrary(player1, List.of(new RevengeOfTheHunted()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Revenge of the Hunted");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void miracleWithoutCreatureTargetsDoesNotSpendMana() {
        harness.setLibrary(player1, List.of(new RevengeOfTheHunted()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Revenge of the Hunted");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
