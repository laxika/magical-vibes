package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheDarkRealms;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErtaiResurrected.class, Forest.class, GrizzlyBears.class, IcyManipulator.class,
        Island.class, LilianaOfTheDarkRealms.class, MerfolkTrickster.class})
class ErtaiResurrectedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and its controller draws a card")
    void countersSpellAndItsControllerDraws() {
        prepareMainPhase(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        castErtai(player2, 0, bears.getId());
        resolveCounterErtai(player2, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Counters an activated ability and its controller draws a card")
    void countersActivatedAbilityAndItsControllerDraws() {
        prepareMainPhase(player1);
        IcyManipulator icyManipulator = new IcyManipulator();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, icyManipulator);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passPriority(player1);

        harness.setLibrary(player1, List.of(new Forest()));
        castErtai(player2, 0, icyManipulator.getId());
        resolveCounterErtai(player2, icyManipulator.getId());

        assertThat(bears.isTapped()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Counters a triggered ability and its controller draws a card")
    void countersTriggeredAbilityAndItsControllerDraws() {
        prepareMainPhase(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MerfolkTrickster trickster = new MerfolkTrickster();
        harness.setHand(player1, List.of(trickster));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passPriority(player1);

        harness.setLibrary(player1, List.of(new Forest()));
        castErtai(player2, 0, trickster.getId());
        resolveCounterErtai(player2, trickster.getId());

        assertThat(bears.isTapped()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Destroys a creature and its controller draws a card")
    void destroysCreatureAndItsControllerDraws() {
        prepareMainPhase(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));

        castErtai(player1, 1, bears.getId());
        resolveErtaiAndTrigger();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys a planeswalker")
    void destroysPlaneswalker() {
        prepareMainPhase(player1);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheDarkRealms());

        castErtai(player1, 1, liliana.getId());
        resolveErtaiAndTrigger();

        harness.assertNotOnBattlefield(player2, "Liliana of the Dark Realms");
    }

    @Test
    @DisplayName("Rejects a land for the destruction mode")
    void rejectsLandForDestructionMode() {
        prepareMainPhase(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ErtaiResurrected()));
        addErtaiMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no ETB mode")
    void canChooseNoMode() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new ErtaiResurrected()));
        addErtaiMana(player1);

        harness.castCreature(player1, 0, -1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ertai Resurrected");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void castErtai(Player player, int mode, java.util.UUID targetId) {
        harness.setHand(player, List.of(new ErtaiResurrected()));
        addErtaiMana(player);
        harness.castCreature(player, 0, mode, targetId);
    }

    private void addErtaiMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private void resolveErtaiAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveCounterErtai(Player controller, java.util.UUID targetId) {
        harness.passBothPriorities();
        harness.handlePermanentChosen(controller, targetId);
        harness.passBothPriorities();
    }
}
