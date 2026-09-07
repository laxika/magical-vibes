package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenProtector.class, Forest.class, GrizzlyBears.class, Shock.class, SuntailHawk.class})
class DenProtectorTest extends BaseCardTest {

    @Test
    void megamorphReturnsTargetCardAndPutsCounterOnDenProtector() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, forest, shock));
        harness.setHand(player1, List.of(new DenProtector()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent protector = findPermanent(player1, "Den Protector");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(protector));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(protector.isFaceDown()).isFalse();
        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, shock);
    }

    @Test
    void creaturesWithLessPowerCannotBlockDenProtector() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent protector = addCreatureReady(player1, new DenProtector());
        protector.setAttacking(true);

        prepareDenProtectorBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(protector);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    void creaturesWithEqualPowerCanBlockDenProtector() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent protector = addCreatureReady(player1, new DenProtector());
        protector.setAttacking(true);

        prepareDenProtectorBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(protector);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void prepareDenProtectorBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
