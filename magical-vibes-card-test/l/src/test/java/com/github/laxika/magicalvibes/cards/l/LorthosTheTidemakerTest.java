package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PillarfieldOx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LorthosTheTidemaker.class, PillarfieldOx.class, Forest.class})
class LorthosTheTidemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {8} taps and locks up to eight target permanents")
    void payingTapsAndLocksEightTargets() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            targets.add(addCreatureReady(player2, new PillarfieldOx()));
        }
        targets.add(harness.addToBattlefieldAndReturn(player2, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, targets.stream().map(Permanent::getId).toList());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(targets).allSatisfy(target -> {
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getSkipUntapCount()).isEqualTo(1);
        });

        advanceToNextTurn(player1);
        assertThat(targets).allMatch(Permanent::isTapped);
        assertThat(targets).allMatch(target -> target.getSkipUntapCount() == 0);
    }

    @Test
    @DisplayName("Declining the payment leaves all targets unchanged")
    void decliningLeavesTargetsUntappedAndUnlocked() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent bear = addCreatureReady(player2, new PillarfieldOx());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId(), forest.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bear.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(bear.getSkipUntapCount()).isZero();
        assertThat(forest.getSkipUntapCount()).isZero();
    }

    @Test
    void alreadyTappedTargetsAndOwnPermanentsSkipOnlyTheirControllersNextUntap() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingOx = addCreatureReady(player2, new PillarfieldOx());
        opposingOx.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(ownForest.getId(), opposingOx.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(opposingOx.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingOx.isTapped()).isTrue();
        assertThat(ownForest.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(ownForest.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingOx.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(ownForest.isTapped()).isFalse();
    }

    @Test
    void untapRestrictionFollowsPermanentToItsNewController() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.performUntapStep(player2);
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void restrictionExpiresEvenIfTargetWasUntappedBeforeItsUntapStep() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        target.untap();
        harness.performUntapStep(player2);
        assertThat(target.getSkipUntapCount()).isZero();
        target.tap();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void insufficientManaDoesNotTapOrLockTargets() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    void choosingZeroTargetsDoesNotRequireAnotherTargetChoiceAfterPayment() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class)
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }

    @Test
    void remainingLegalTargetsResolveEvenAfterLorthosAndAnotherTargetLeave() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(removedTarget.getId(), remainingTarget.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(lorthos);
        gd.playerGraveyards.get(player1.getId()).add(lorthos.getCard());
        gd.playerBattlefields.get(player2.getId()).remove(removedTarget);
        gd.playerGraveyards.get(player2.getId()).add(removedTarget.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(remainingTarget.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remainingTarget.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remainingTarget.isTapped()).isFalse();
    }

    @Test
    void allTargetsLeavingPreventsThePaymentChoice() {
        Permanent lorthos = addCreatureReady(player1, new LorthosTheTidemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(target.getSkipUntapCount()).isZero();
    }
}
