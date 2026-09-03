package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodenSphere.class, GrizzlyBears.class, GrayOgre.class})
class WoodenSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casts green spell, pays {1}, gains 1 life")
    void controllerCastsGreenSpellAndPays() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new WoodenSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int manaBeforePayment = gd.playerManaPools.get(player1.getId()).getTotal();

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && sphere.getId().equals(e.getSourcePermanentId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(manaBeforePayment - 1);
    }

    @Test
    @DisplayName("Controller casts green spell, declines to pay, no life gain")
    void controllerCastsGreenSpellAndDeclines() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casts green spell, controller pays {1}, gains 1 life")
    void opponentCastsGreenSpellControllerPays() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-green spell does not trigger Wooden Sphere")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.castFromHand(player1, new GrayOgre(), "{2}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Triggered ability resolves after Wooden Sphere leaves the battlefield")
    void triggeredAbilityResolvesAfterSphereLeavesBattlefield() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new WoodenSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.playerBattlefields.get(player1.getId()).remove(sphere)).isTrue();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }
}
