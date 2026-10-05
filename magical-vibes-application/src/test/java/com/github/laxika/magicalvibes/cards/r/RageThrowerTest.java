package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.g.GarrukRelentless;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageThrower.class, GrizzlyBears.class, Shock.class, BlasphemousAct.class,
        BrimstoneVolley.class, DarkthicketWolf.class, GarrukRelentless.class})
class RageThrowerTest extends BaseCardTest {

    @Test
    @DisplayName("When an ally creature dies, Rage Thrower deals 2 damage to target player")
    void allyCreatureDeathDeals2Damage() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill ally creature with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player takes 2 damage
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("When an opponent's creature dies, Rage Thrower deals 2 damage to target player")
    void opponentCreatureDeathDeals2Damage() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player takes 2 damage
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Rage Thrower does NOT trigger when it dies itself")
    void doesNotTriggerOnOwnDeath() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill Rage Thrower with Shock (4/2 creature, 2 damage is lethal)
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID throwerId = harness.getPermanentId(player1, "Rage Thrower");
        harness.castAndResolveInstant(player2, 0, throwerId);

        // No death trigger target selection should occur — life totals unchanged
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger can target the controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Controller takes 2 damage
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({RageThrower.class, DarkthicketWolf.class, BrimstoneVolley.class, GarrukRelentless.class})
    @DisplayName("Death trigger can target a planeswalker")
    void deathTriggerCanTargetPlaneswalker() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.addToBattlefield(player2, new DarkthicketWolf());
        harness.addToBattlefield(player2, new GarrukRelentless());
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Darkthicket Wolf"));
        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Garruk Relentless"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({RageThrower.class, DarkthicketWolf.class, BlasphemousAct.class})
    @DisplayName("Triggers for each other creature dying simultaneously with Rage Thrower")
    void simultaneousDeathsStillTriggerForEachOtherCreature() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.addToBattlefield(player2, new DarkthicketWolf());
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rage Thrower");
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
