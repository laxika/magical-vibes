package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZelyonSword;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiGodOfMischief.class, ProdigalPyromancer.class, ZelyonSword.class, GrizzlyBears.class, Shock.class,
        Nekrataal.class, Humility.class, Gravedigger.class})
class LokiGodOfMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws only once when its controller's abilities target players")
    void drawsOnlyOnceForControllerAbilitiesTargetingPlayers() {
        addReadyProdigalPyromancer(player1);
        addReadyProdigalPyromancer(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when its controller's ability targets a permanent")
    void drawsForControllerAbilityTargetingPermanent() {
        Permanent sword = addReadySword(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for a targeted spell")
    void doesNotTriggerForTargetedSpell() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an ability controlled by an opponent")
    void doesNotTriggerForOpponentAbility() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        addReadyProdigalPyromancer(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Draws when a controlled triggered ability targets a creature")
    void drawsForControlledTriggeredAbility() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activated and triggered abilities share the once-per-turn limit")
    void activatedAndTriggeredAbilitiesShareLimit() {
        addReadyProdigalPyromancer(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The draw trigger can fire again on the opponent's turn")
    void drawsAgainOnOpponentsTurn() {
        addReadyProdigalPyromancer(player1);
        addReadyProdigalPyromancer(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw while Humility removes Loki's ability")
    void doesNotDrawWhenContinuousEffectRemovesAbility() {
        addReadySword(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.addToBattlefield(player2, new Humility());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting a creature card in a graveyard does not trigger Loki")
    void doesNotDrawForGraveyardCardTarget() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
    }

    private Permanent addReadyProdigalPyromancer(Player player) {
        Permanent permanent = addCreatureReady(player, new ProdigalPyromancer());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadySword(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ZelyonSword());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
