package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorshellCrab.class, Shock.class, ProdigalSorcerer.class})
class MirrorshellCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay {3}")
    void wardCountersUnpaidSpell() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        castShockAtCrab(crab, 1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Mirrorshell Crab");
        assertThat(crab.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Paying {3} lets an opponent's spell targeting Mirrorshell Crab resolve")
    void payingWardLetsSpellResolve() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        castShockAtCrab(crab, 4);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(crab.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Channel counters an activated ability unless its controller pays {3}")
    void channelCountersActivatedAbility() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.setHand(player1, List.of(new MirrorshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, sorcerer.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Mirrorshell Crab");
        assertThat(gd.stack).isEmpty();
    }

    private void castShockAtCrab(Permanent crab, int colorlessMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, colorlessMana);

        harness.castInstant(player2, 0, crab.getId());
    }
}
