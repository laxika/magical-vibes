package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodenSphere.class, GrizzlyBears.class, AirElemental.class, GiantGrowth.class})
class WoodenSphereTest extends BaseCardTest {
    @Test
    @DisplayName("Controller casts green spell, pays {1}, gains 1 life")
    void controllerCastsGreenSpellAndPays() {
        harness.addToBattlefield(player1, new WoodenSphere());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Wooden Sphere"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Controller casts green spell, declines to pay, no life gain")
    void controllerCastsGreenSpellAndDeclines() {
        harness.addToBattlefield(player1, new WoodenSphere());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new WoodenSphere());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore);
    }
    @Test
    @DisplayName("Opponent casts green spell, controller pays {1}, gains 1 life")
    void opponentCastsGreenSpellControllerPays() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertLife(player2, opponentLifeBefore);
    }
    @Test
    @DisplayName("Non-green spell does not trigger Wooden Sphere")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Wooden Sphere"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Green noncreature spell triggers Wooden Sphere")
    void greenNoncreatureSpellTriggers() {
        harness.addToBattlefield(player1, new WoodenSphere());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each Wooden Sphere triggers independently for one green spell")
    void multipleWoodenSpheresTriggerIndependently() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.addToBattlefield(player1, new WoodenSphere());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Wooden Sphere")))
                .hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Wooden Sphere does not trigger while in its owner's hand")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setHand(player1, List.of(new WoodenSphere(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 1);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Wooden Sphere"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
