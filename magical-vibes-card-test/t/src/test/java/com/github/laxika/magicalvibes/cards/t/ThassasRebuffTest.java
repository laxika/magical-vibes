package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThassasRebuff.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class ThassasRebuffTest extends BaseCardTest {

    private void castRebuffOnBears(GrizzlyBears bears, int extraMana) {
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2 + extraMana);

        harness.setHand(player2, List.of(new ThassasRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
    }

    @Test
    @DisplayName("Counters the spell when its controller cannot pay blue devotion")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player2, new AirElemental()); // Two blue mana symbols -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        castRebuffOnBears(bears, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lets the spell resolve when its controller pays the blue devotion cost")
    void notCounteredWhenControllerPays() {
        harness.addToBattlefield(player2, new AirElemental()); // Two blue mana symbols -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        castRebuffOnBears(bears, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller may decline payment even when they can afford it")
    void countersWhenControllerDeclinesPayment() {
        harness.addToBattlefield(player2, new AirElemental());
        castRebuffOnBears(new GrizzlyBears(), 2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With zero devotion the controller can pay zero and keep the spell")
    void zeroDevotionAllowsZeroPayment() {
        castRebuffOnBears(new GrizzlyBears(), 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Zero devotion still allows the controller to decline paying zero")
    void zeroDevotionAllowsDecliningPayment() {
        castRebuffOnBears(new GrizzlyBears(), 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the Rebuff controller's permanents contribute to devotion")
    void opponentsBluePermanentsDoNotContribute() {
        harness.addToBattlefield(player1, new AirElemental());
        castRebuffOnBears(new GrizzlyBears(), 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Devotion is evaluated at resolution rather than when Rebuff is cast")
    void devotionChangesBeforeResolution() {
        castRebuffOnBears(new GrizzlyBears(), 0);
        harness.addToBattlefield(player2, new AirElemental());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapped lands allow mana payment during Rebuff's resolution")
    void controllerCanGenerateManaDuringResolution() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castRebuffOnBears(new GrizzlyBears(), 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
