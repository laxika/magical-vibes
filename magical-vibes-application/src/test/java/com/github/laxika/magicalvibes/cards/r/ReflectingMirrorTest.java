package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReflectingMirror.class, Shock.class, GrizzlyBears.class})
class ReflectingMirrorTest extends BaseCardTest {

    @Test
    void changesTargetToAnotherPlayer() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void newTargetMustBeAPlayer() {
        Shock shock = new Shock();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, shock.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void requiresTwiceTargetSpellManaValue() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyTargetASpellThatTargetsTheMirrorController() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
