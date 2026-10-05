package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OhabiCaleria.class, GrizzlyBears.class, Forest.class, FemerefArchers.class,
        SkyhunterSkirmisher.class, Shock.class, Humility.class})
class OhabiCaleriaTest extends BaseCardTest {

    @Test
    @DisplayName("Ohabi untaps your Archers during an opponent's untap step")
    void untapsYourArchersDuringOpponentsUntapStep() {
        Permanent ohabi = addCreatureReady(player1, new OhabiCaleria());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        ohabi.tap();
        bears.tap();
        forest.tap();

        harness.performUntapStep(player2);

        assertThat(ohabi.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Archer dealing damage to a creature may be paid for to draw")
    void archerDamageMayDraw() {
        Permanent ohabi = addCreatureReady(player1, new OhabiCaleria());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        ohabi.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Archer dealing damage to a creature may decline the draw")
    void archerDamageMayDeclineDraw() {
        Permanent ohabi = addCreatureReady(player1, new OhabiCaleria());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        ohabi.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An Archer dealing noncombat damage to a creature may be paid for to draw")
    void archerNoncombatDamageMayDraw() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(archers.isTapped()).isTrue();
    }

    @Test
    void untapsOtherArchersButNotOpponentsArchers() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent ally = addCreatureReady(player1, new FemerefArchers());
        Permanent opponent = addCreatureReady(player2, new FemerefArchers());
        ally.tap();
        opponent.tap();

        harness.performUntapStep(player2);

        assertThat(ally.isTapped()).isFalse();
        opponent.tap();
        harness.performUntapStep(player1);
        assertThat(opponent.isTapped()).isTrue();
    }

    @Test
    void nonArcherDamageDoesNotTrigger() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsArcherDamageDoesNotTrigger() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new FemerefArchers());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void damageToPlayerDoesNotTrigger() {
        Permanent ohabi = addCreatureReady(player1, new OhabiCaleria());
        ohabi.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingAbilitiesPreventsDamageTrigger() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Humility());
        archers.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void otherArcherStillTriggersWhenOhabiDiesInSameCombatDamageStep() {
        Permanent ohabi = addCreatureReady(player1, new OhabiCaleria());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, ohabi.getId());
        harness.passBothPriorities();
        ohabi.setAttacking(true);
        archers.setAttacking(true);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ohabi, archers);
        assertThat(gd.stack).hasSize(2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void archerAbilityStillTriggersAfterArcherLeavesBattlefield() {
        addCreatureReady(player1, new OhabiCaleria());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.castInstant(player2, 0, archers.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(archers);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
