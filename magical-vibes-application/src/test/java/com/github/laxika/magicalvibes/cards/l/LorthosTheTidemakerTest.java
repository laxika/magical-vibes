package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LorthosTheTidemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {8} taps and locks up to eight target permanents")
    void payingTapsAndLocksEightTargets() {
        Permanent lorthos = addReady(player1, new LorthosTheTidemaker());
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            targets.add(addReady(player2, new GrizzlyBears()));
        }
        targets.add(addReady(player2, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }

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
        Permanent lorthos = addReady(player1, new LorthosTheTidemaker());
        Permanent bear = addReady(player2, new GrizzlyBears());
        Permanent forest = addReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lorthos)));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bear.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(bear.getSkipUntapCount()).isZero();
        assertThat(forest.getSkipUntapCount()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
