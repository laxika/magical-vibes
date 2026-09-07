package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoyaDeathFromAbove.class, GrizzlyBears.class})
class KoyaDeathFromAboveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and returns it when the controller declines to pay")
    void declinesPaymentAndReturnsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKoyaTargeting(bears);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();

        advanceToNextEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("Paying {3}{B} keeps the exiled creature in exile")
    void paysAndKeepsCreatureExiled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKoyaTargeting(bears);

        advanceToNextEndStep();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castKoyaTargeting(Permanent target) {
        harness.setHand(player1, List.of(new KoyaDeathFromAbove()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextEndStep() {
        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
