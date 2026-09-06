package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.i.Inquisition;
import com.github.laxika.magicalvibes.cards.s.Squire;
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

@CardUsed({ReflectingMirror.class, Inquisition.class, Squire.class, ArcTrail.class})
class ReflectingMirrorTest extends BaseCardTest {

    @Test
    void changesTargetToAnotherPlayer() {
        Inquisition inquisition = new Inquisition();
        harness.setHand(player1, List.of(inquisition, new Squire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, inquisition.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void newTargetMustBeAPlayer() {
        Inquisition inquisition = new Inquisition();
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new Squire());
        harness.setHand(player1, List.of(inquisition));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, inquisition.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, squire.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void requiresTwiceTargetSpellManaValue() {
        Inquisition inquisition = new Inquisition();
        harness.setHand(player1, List.of(inquisition));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, inquisition.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyTargetASpellThatTargetsTheMirrorController() {
        Inquisition inquisition = new Inquisition();
        harness.setHand(player1, List.of(inquisition));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, inquisition.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSpellWithMultipleTargets() {
        ArcTrail arcTrail = new ArcTrail();
        harness.setHand(player1, List.of(arcTrail));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent mirror = harness.addToBattlefieldAndReturn(player2, new ReflectingMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of(player2.getId(), player1.getId()));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(mirror), null, arcTrail.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
