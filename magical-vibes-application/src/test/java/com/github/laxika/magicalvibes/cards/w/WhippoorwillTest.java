package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeathcurseOgre;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OraclesAttendants;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShallowGrave;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whippoorwill.class, DrudgeSkeletons.class, Shock.class, OraclesAttendants.class,
        DeathcurseOgre.class,
        GrizzlyBears.class, FountainOfYouth.class, TrainedArmodon.class, ShallowGrave.class})
class WhippoorwillTest extends BaseCardTest {

    @Test
    @DisplayName("The ability makes damage lethal, prevents regeneration, then exiles the creature")
    void marksTargetForDamageAndRegenerationRestrictions() {
        addCreatureReady(player1, new Whippoorwill());
        Permanent target = addCreatureReady(player2, new DrudgeSkeletons());
        target.setDamagePreventionShield(5);
        target.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isDamageCantBePreventedOrRedirectedThisTurn()).isTrue();
        assertThat(target.isCantRegenerateThisTurn()).isTrue();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Drudge Skeletons"));
        assertThat(target.getDamagePreventionShield()).isEqualTo(5);
    }

    @Test
    void deathTriggerSeesMarkedCreatureInGraveyard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Whippoorwill());
        Permanent target = addCreatureReady(player2, new DeathcurseOgre());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        target.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The marked creature still receives combat damage that would be redirected")
    void damageCannotBeRedirectedAwayFromMarkedCreature() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent target = addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player1, new Whippoorwill());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 2, null, target.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(attendants.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("All three marks clear during end-of-turn cleanup")
    void marksClearAtEndOfTurn() {
        addCreatureReady(player1, new Whippoorwill());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isDamageCantBePreventedOrRedirectedThisTurn()).isFalse();
        assertThat(target.isCantRegenerateThisTurn()).isFalse();
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new Whippoorwill());
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void originalDeathTriggerDoesNotExileCreatureAfterItReturnsAndDiesAgain() {
        addCreatureReady(player1, new Whippoorwill());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.castFromHand(player2, new ShallowGrave(), "{1}{B}");
        harness.passBothPriorities();
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.isCantRegenerateThisTurn()).isFalse();
        assertThat(returned.isDamageCantBePreventedOrRedirectedThisTurn()).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void delayedExileStillWorksAfterWhippoorwillDies() {
        Permanent source = addCreatureReady(player1, new Whippoorwill());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        source.setMarkedDamage(1);
        harness.runStateBasedActions();
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }
}
