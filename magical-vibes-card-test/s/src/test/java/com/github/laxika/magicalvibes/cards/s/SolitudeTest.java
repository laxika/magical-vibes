package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Solitude.class, GrizzlyBears.class, Forest.class})
class SolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and its controller gains life equal to its power")
    void exilesAnotherCreatureAndItsControllerGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveSolitude();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Can enter without choosing a creature")
    void canEnterWithoutChoosingACreature() {
        harness.castFromHand(player1, new Solitude(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Solitude");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        giveSolitude();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Evoke exiles a white card, exiles the target, and sacrifices Solitude")
    void evokeExilesCreatureAndSacrificesSolitude() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        Solitude payment = new Solitude();
        harness.setHand(player1, List.of(new Solitude(), payment));

        harness.castInstantWithAlternateExileFromHand(player1, 0, target.getId(), 1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Solitude");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("One white mana cannot replace the required white card evoke payment")
    void cannotEvokeForWhiteManaAlone() {
        harness.setHand(player1, List.of(new Solitude()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May decline to exile a creature even when a legal target exists")
    void canChooseNoTargetWithCreatureAvailable() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Solitude(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Solitude");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can exile its controller's other creature and give that controller life")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        giveSolitude();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.assertOnBattlefield(player1, "Solitude");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain uses the target's power when the ability resolves")
    void usesPowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveSolitude();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(3);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Exiling a creature with negative power does not decrease its controller's life")
    void negativePowerGainsNoLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(-3);
        giveSolitude();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player2, 20);
    }

    private void giveSolitude() {
        harness.setHand(player1, List.of(new Solitude()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
