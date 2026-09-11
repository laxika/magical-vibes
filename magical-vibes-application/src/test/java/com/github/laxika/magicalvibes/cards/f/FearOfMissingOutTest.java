package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FearOfMissingOut.class, GrizzlyBears.class, Forest.class, Shock.class,
        LeoninScimitar.class, Pacifism.class})
class FearOfMissingOutTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller discards a card and draws a card")
    void discardsAndDrawsOnEntry() {
        harness.setHand(player1, List.of(new FearOfMissingOut(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("The first delirious attack untaps a target creature and creates an extra combat")
    void firstDeliriousAttackUntapsTargetAndCreatesExtraCombat() {
        Permanent fear = addCreatureReady(player1, new FearOfMissingOut());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        setDelirium();

        declareAttack(fear);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Without delirium, the attack trigger does not trigger or ask for a target")
    void doesNotTriggerWithoutDelirium() {
        Permanent fear = addCreatureReady(player1, new FearOfMissingOut());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new LeoninScimitar()));

        declareAttack(fear);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The delirium condition is checked again when the attack trigger resolves")
    void rechecksDeliriumAtResolution() {
        Permanent fear = addCreatureReady(player1, new FearOfMissingOut());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        setDelirium();

        declareAttack(fear);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents both the untap and the additional combat")
    void illegalTargetPreventsBothEffects() {
        Permanent fear = addCreatureReady(player1, new FearOfMissingOut());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        setDelirium();

        declareAttack(fear);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    private void declareAttack(Permanent fear) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(fear)));
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
