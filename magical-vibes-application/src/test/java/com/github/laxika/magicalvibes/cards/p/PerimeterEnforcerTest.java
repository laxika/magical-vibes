package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AxebaneFerox;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.u.UndercoverCrocodelf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerimeterEnforcer.class, AxebaneFerox.class, DogWalker.class, NoviceInspector.class,
        UndercoverCrocodelf.class, AmoeboidChangeling.class})
class PerimeterEnforcerTest extends BaseCardTest {

    @Test
    void detectiveEnteringBoostsPerimeterEnforcerUntilEndOfTurn() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void nonDetectiveEnteringDoesNotTriggerPerimeterEnforcer() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new AxebaneFerox()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void detectiveTurnedFaceUpBoostsPerimeterEnforcer() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new UndercoverCrocodelf()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent detective = findPermanent(player1, "Undercover Crocodelf");
        assertThat(detective.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(detective));
        resolveAllTriggers();

        assertThat(detective.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
    }

    @Test
    void enteringItselfDoesNotTrigger() {
        harness.setHand(player1, List.of(new PerimeterEnforcer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent enforcer = findPermanent(player1, "Perimeter Enforcer");
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void opponentDetectiveEnteringDoesNotTrigger() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NoviceInspector()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void multipleDetectivesGiveCumulativeBoosts() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new NoviceInspector(), new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);
    }

    @Test
    void opponentDetectiveTurningFaceUpDoesNotTrigger() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new UndercoverCrocodelf()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        resolveAllTriggers();
        Permanent detective = findPermanent(player2, "Undercover Crocodelf");
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(detective));
        resolveAllTriggers();

        assertThat(detective.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void nonDetectiveTurningFaceUpDoesNotTrigger() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent citizen = findPermanent(player1, "Dog Walker");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(citizen));
        resolveAllTriggers();

        assertThat(citizen.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void creatureThatLostDetectiveDoesNotTriggerWhenTurnedFaceUp() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        Permanent changeling = addCreatureReady(player1, new AmoeboidChangeling());
        harness.setHand(player1, List.of(new UndercoverCrocodelf()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent creature = findPermanent(player1, "Undercover Crocodelf");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(changeling),
                1, null, creature.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature));
        resolveAllTriggers();

        assertThat(creature.isFaceDown()).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DETECTIVE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void creatureThatGainedDetectiveTriggersWhenTurnedFaceUp() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        Permanent changeling = addCreatureReady(player1, new AmoeboidChangeling());
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent creature = findPermanent(player1, "Dog Walker");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(changeling),
                0, null, creature.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature));
        resolveAllTriggers();

        assertThat(creature.isFaceDown()).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DETECTIVE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
    }
}
