package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerennialGravewarden.class, GrizzlyBears.class, Shock.class})
class PerennialGravewardenTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually gets +1/+1 when it enters")
    void perpetuallyBoostsItselfWhenEntering() {
        Permanent gravewarden = harness.enterBattlefieldAndReturn(player1, new PerennialGravewarden());

        assertThat(gqs.getEffectivePower(gd, gravewarden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gravewarden)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns tapped from the graveyard when a creature entered and died this turn")
    void returnsTappedAfterCreatureEnteredAndDied() {
        Card gravewarden = new PerennialGravewarden();
        harness.setGraveyard(player1, List.of(gravewarden));

        Permanent bear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWithShock(bear.getId());
        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gravewarden);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(gravewarden.getId())
                        && permanent.isTapped());
    }

    @Test
    @DisplayName("Does not return when either turn condition is missing")
    void doesNotReturnWithoutBothEvents() {
        Card gravewarden = new PerennialGravewarden();
        harness.setGraveyard(player1, List.of(gravewarden));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWithShock(bear.getId());
        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gravewarden);
    }

    private void destroyWithShock(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
