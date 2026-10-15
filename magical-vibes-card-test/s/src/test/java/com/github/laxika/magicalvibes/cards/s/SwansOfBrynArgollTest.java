package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwansOfBrynArgoll.class, Shock.class, GrizzlyBears.class, HillGiant.class})
class SwansOfBrynArgollTest extends BaseCardTest {

    @Test
    @DisplayName("Burn spell to Swans is prevented and the source's controller draws that many cards")
    void burnDamagePreventedSourceControllerDraws() {
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, swans.getId());

        // Swans took no damage and survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(swans.getId()));
        assertThat(swans.getMarkedDamage()).isZero();
        // Shock's controller (player1) drew 2 cards; Swans' controller (player2) drew nothing.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Lethal combat damage to Swans is prevented and the attacker's controller draws")
    void combatDamagePreventedSourceControllerDraws() {
        // A 3/3 attacker blocked by the 4/3 Swans would deal lethal (3) damage to it.
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        swans.setSummoningSick(false);
        swans.setBlocking(true);
        swans.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The 3 (lethal) combat damage to Swans was prevented — it survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(swans.getId()));
        assertThat(swans.getMarkedDamage()).isZero();
        // The attacker's controller (player1) drew 3 cards.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("When damage can't be prevented, Swans takes damage and the source's controller draws nothing")
    void unpreventableDamageStillLandsNoDraw() {
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        gd.damageCantBePreventedThisTurn = true;

        harness.castAndResolveInstant(player1, 0, swans.getId());

        assertThat(swans.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @CardUsed({Snakeform.class})
    @DisplayName("Swans that has lost its abilities takes damage without making the source controller draw")
    void abilityLossDisablesPreventionAndDrawing() {
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Snakeform(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, swans.getId());
        harness.castAndResolveInstant(player1, 0, swans.getId());

        harness.assertNotOnBattlefield(player2, "Swans of Bryn Argoll");
        harness.assertInGraveyard(player2, "Swans of Bryn Argoll");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damaging your own Swans also prevents damage and draws cards")
    void ownSourceAndSwansControllerDraws() {
        Permanent swans = harness.addToBattlefieldAndReturn(player1, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, swans.getId());

        harness.assertOnBattlefield(player1, "Swans of Bryn Argoll");
        assertThat(swans.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({ProdigalPyromancer.class, RayOfCommand.class})
    @DisplayName("The damage source's current controller draws after control changes with its ability on the stack")
    void currentSourceControllerDrawsRatherThanAbilityController() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, swans.getId());
        harness.castAndResolveInstant(player2, 0, pyromancer.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
        harness.passBothPriorities();

        assertThat(swans.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
