package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
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

@CardUsed({TeferisProtection.class, GrizzlyBears.class, Shock.class, CentaurHealer.class})
class TeferisProtectionTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out your permanents, locks your life total, grants protection, and exiles itself")
    void resolvesAllEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).contains(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).contains(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Teferi's Protection"));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");
    }

    @Test
    @DisplayName("Life gain is prevented until your next turn")
    void lifeGainIsPreventedUntilNextTurn() {
        harness.setLife(player1, 10);
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        harness.setHand(player1, List.of(new CentaurHealer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Protection and life lock end at your next turn")
    void effectsEndAtNextTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).contains(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).contains(player1.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isTrue();
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
