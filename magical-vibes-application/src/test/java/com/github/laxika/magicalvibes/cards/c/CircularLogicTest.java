package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircularLogic.class, FuneralCharm.class, GrizzlyBears.class, LavaAxe.class, LightningBolt.class, RavensCrime.class, Shock.class})
class CircularLogicTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay for the cards in your graveyard")
    void countersWhenControllerCannotPay() {
        harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CircularLogic()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not counter a spell when its controller pays the graveyard-scaled cost")
    void doesNotCounterWhenControllerPays() {
        harness.setGraveyard(player2, List.of(new Shock()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new CircularLogic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts Circular Logic into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        CircularLogic logic = discardViaRavensCrime();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Circular Logic");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(logic.getId()));
    }

    @Test
    @DisplayName("Madness casts Circular Logic for {U} and counters a spell on the stack")
    void acceptingMadnessCountersTargetSpell() {
        harness.setGraveyard(player1, List.of(new LavaAxe()));
        CircularLogic logic = new CircularLogic();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(logic, new FuneralCharm()));
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Circular Logic");
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private CircularLogic discardViaRavensCrime() {
        CircularLogic logic = new CircularLogic();
        harness.setHand(player1, List.of(logic));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return logic;
    }
}
