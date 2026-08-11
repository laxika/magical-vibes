package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrumarBondKin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemurChargerTest extends BaseCardTest {

    @Test
    void turningFaceUpByRevealingAGreenCardGivesTargetCreatureTrampleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears greenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TemurCharger(), greenCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger), 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(charger.isFaceDown()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutRevealingAGreenCard() {
        harness.setHand(player1, List.of(new TemurCharger(), new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be green card");
        assertThat(charger.isFaceDown()).isTrue();
    }
}
