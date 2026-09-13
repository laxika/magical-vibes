package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisProtection.class, GrizzlyBears.class, Shock.class})
class TeferisProtectionTest extends BaseCardTest {

    @Test
    @DisplayName("Teferi's Protection locks life, protects its controller, phases out permanents, and exiles itself")
    void resolvesAllEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        castProtection();

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).contains(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).contains(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Teferi's Protection");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");

        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Teferi's Protection ends at its controller's next turn")
    void effectsEndAtNextTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        castProtection();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void castProtection() {
        harness.setHand(player1, List.of(new TeferisProtection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
