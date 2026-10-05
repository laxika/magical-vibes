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
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
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

    @Test
    @DisplayName("Ward counters an opponent's activated ability without moving its source")
    void wardCountersActivatedAbility() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, crab.getId());
        harness.passBothPriorities();

        assertThat(crab.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent can decline ward payment despite having enough mana")
    void decliningWardCountersSpell() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        castShockAtCrab(crab, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(crab.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, crab.getId());
        harness.passBothPriorities();

        assertThat(crab.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel counters a creature spell with no targets and discards as a cost")
    void channelCountersUntargetedSpell() {
        MirrorshellCrab spell = new MirrorshellCrab();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, spell, "{5}{U}{U}");
        harness.setHand(player1, List.of(new MirrorshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.activateHandAbility(player1, 0, spell.getId());
        harness.assertInGraveyard(player1, "Mirrorshell Crab");
        harness.assertNotInHand(player1, "Mirrorshell Crab");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mirrorshell Crab");
        harness.assertNotOnBattlefield(player2, "Mirrorshell Crab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying Channel's {3} lets the targeted spell resolve")
    void payingChannelLetsSpellResolve() {
        MirrorshellCrab spell = new MirrorshellCrab();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, spell, "{5}{U}{U}");
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MirrorshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, spell.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mirrorshell Crab");
        harness.assertNotInGraveyard(player2, "Mirrorshell Crab");
        harness.assertInGraveyard(player1, "Mirrorshell Crab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel can counter its controller's non-targeting ward trigger")
    void channelCountersWardTrigger() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new MirrorshellCrab());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new MirrorshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, crab.getId());
        var wardId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.activateHandAbility(player1, 0, wardId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crab.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mirrorshell Crab");
        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
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
