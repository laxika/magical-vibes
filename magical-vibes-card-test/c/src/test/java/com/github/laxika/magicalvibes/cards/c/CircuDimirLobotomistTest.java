package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DimirInfiltrator;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.r.RoofstalkerWight;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
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

@CardUsed({CircuDimirLobotomist.class, DimirInfiltrator.class, ElvesOfDeepShadow.class,
        Lignify.class, LastGasp.class, RoofstalkerWight.class, SnappingDrake.class})
class CircuDimirLobotomistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a blue spell exiles the top card of the chosen player's library with Circu")
    void blueSpellExilesTopCardWithCircu() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        RoofstalkerWight topCard = new RoofstalkerWight();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a black spell exiles the top card of the chosen player's library with Circu")
    void blackSpellExilesTopCardWithCircu() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        SnappingDrake topCard = new SnappingDrake();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new RoofstalkerWight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Casting a blue and black spell triggers both of Circu's abilities")
    void blueAndBlackSpellTriggersBothAbilities() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        SnappingDrake controllerTopCard = new SnappingDrake();
        RoofstalkerWight opponentTopCard = new RoofstalkerWight();
        harness.setLibrary(player1, List.of(controllerTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new DimirInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreature(player1, 0);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player1.getId());
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
            PendingInteraction.ColorChoice triggerOrder = gd.interaction
                    .activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(triggerOrder).isNotNull();
            harness.handleListChoice(player1, triggerOrder.options().getFirst());
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gd.getCardsExiledByPermanent(circu.getId()))
                .containsExactlyInAnyOrder(controllerTopCard, opponentTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponents cannot cast a spell with the name of a card exiled with Circu")
    void opponentCannotCastSpellWithExiledName() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        ElvesOfDeepShadow exiledCard = new ElvesOfDeepShadow();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(exiledCard);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ElvesOfDeepShadow()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Circu's controller can cast a spell with the name of a card exiled with Circu")
    void controllerCanCastSpellWithExiledName() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        ElvesOfDeepShadow exiledCard = new ElvesOfDeepShadow();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(exiledCard);

        harness.setHand(player1, List.of(new ElvesOfDeepShadow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elves of Deep Shadow");
    }

    @Test
    @DisplayName("Circu's restriction ends when Circu loses all abilities")
    void restrictionEndsWhenCircuLosesAllAbilities() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        ElvesOfDeepShadow exiledCard = new ElvesOfDeepShadow();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(exiledCard);

        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, circu.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, circu)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ElvesOfDeepShadow()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elves of Deep Shadow");
    }

    @Test
    @DisplayName("A green spell does not trigger Circu despite its black color identity")
    void greenSpellDoesNotTrigger() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        SnappingDrake topCard = new SnappingDrake();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new ElvesOfDeepShadow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elves of Deep Shadow");
        assertThat(gd.getCardsExiledByPermanent(circu.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An opponent's blue and black spell does not trigger Circu")
    void opponentSpellDoesNotTrigger() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        SnappingDrake topCard = new SnappingDrake();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DimirInfiltrator()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dimir Infiltrator");
        assertThat(gd.getCardsExiledByPermanent(circu.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Circu can target an empty library and the trigger does nothing")
    void emptyLibraryDoesNotPreventSpellResolving() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Snapping Drake");
        assertThat(gd.getCardsExiledByPermanent(circu.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A pending Circu trigger still exiles a card after Circu dies")
    void triggerResolvesAfterSourceDies() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        ElvesOfDeepShadow topCard = new ElvesOfDeepShadow();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player2, 0, circu.getId());
        harness.assertNotOnBattlefield(player1, "Circu, Dimir Lobotomist");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Snapping Drake");
    }

    @Test
    @DisplayName("Circu's casting restriction ends when Circu dies but the card stays exiled")
    void restrictionEndsWhenSourceDies() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        ElvesOfDeepShadow exiledCard = new ElvesOfDeepShadow();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(exiledCard);

        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, circu.getId());
        harness.assertNotOnBattlefield(player1, "Circu, Dimir Lobotomist");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ElvesOfDeepShadow()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elves of Deep Shadow");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }
}
