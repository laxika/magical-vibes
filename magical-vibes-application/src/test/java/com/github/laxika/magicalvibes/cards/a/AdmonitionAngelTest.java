package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdmonitionAngel.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class AdmonitionAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may exile another nonland permanent")
    void landfallExilesTargetPermanent() {
        addAngel();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Admonition Angel").getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining landfall leaves the permanent on the battlefield")
    void decliningLandfallDoesNothing() {
        addAngel();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiled permanents return under their owners' control when the Angel leaves")
    void exiledPermanentsReturnWhenAngelLeaves() {
        Permanent angel = addAngel();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(angel.getId())).isEmpty();
    }

    @Test
    @DisplayName("Landfall cannot target a land or the Angel itself")
    void landfallRejectsIllegalTargets() {
        Permanent angel = addAngel();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void landfallStillExilesAfterAngelLeaves() {
        Permanent angel = addAngel();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.castAndResolveInstant(player1, 0, angel.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Admonition Angel");
        assertThat(gd.getCardsExiledByPermanent(angel.getId()))
                .containsExactly(bears.getCard());
    }

    @Test
    void multipleExiledCardsReturnToTheirOwners() {
        Permanent angel = addAngel();
        Permanent stolenBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolenBears.getId(), player2.getId());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, stolenBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.handlePermanentChosen(player1, ownBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(angel.getId()))
                .containsExactlyInAnyOrder(stolenBears.getCard(), ownBears.getCard());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(ownBears.getCard()).doesNotContain(stolenBears.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).contains(stolenBears.getCard()).doesNotContain(ownBears.getCard());
        assertThat(gd.getCardsExiledByPermanent(angel.getId())).isEmpty();
    }

    @Test
    void opponentsLandDoesNotTriggerAngel() {
        addAngel();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addAngel() {
        return harness.addToBattlefieldAndReturn(player1, new AdmonitionAngel());
    }
}
