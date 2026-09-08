package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KuonTest extends BaseCardTest {

    @Test
    @DisplayName("Flips at the end step after three creatures die this turn")
    void flipsAfterThreeCreatureDeaths() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (Permanent target : List.of(first, second, third)) {
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(kuon.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip at the end step after fewer than three creatures die")
    void doesNotFlipAfterOnlyTwoCreatureDeaths() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (Permanent target : List.of(first, second)) {
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep();

        assertThat(kuon.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Kuon's Essence makes the active player sacrifice a creature at each upkeep")
    void essenceSacrificesCreatureOfActivePlayerAtUpkeep() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        kuon.setTransformed(true);
        kuon.setCard(kuon.getOriginalCard().getBackFaceCard());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
