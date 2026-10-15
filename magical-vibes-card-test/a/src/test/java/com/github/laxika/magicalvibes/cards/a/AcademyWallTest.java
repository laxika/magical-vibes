package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CaptainsCall;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AcademyWall.class, CaptainsCall.class, Forest.class, GrizzlyBears.class, Shock.class})
class AcademyWallTest extends BaseCardTest {

    @Test
    @DisplayName("May draw a card, then discard a card when an instant is cast")
    void mayDrawThenDiscardWhenInstantIsCast() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the trigger does not draw or discard")
    void decliningTriggerDoesNothing() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Sorcery casts also allow drawing and discarding")
    void sorceryCastAllowsLooting() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new CaptainsCall(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Creature casts do not trigger or consume the once-per-turn limit")
    void creatureCastDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Opponent casts do not trigger or consume the controller's trigger")
    void opponentCastDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 18);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The once-per-turn limit resets on the opponent's turn")
    void triggerResetsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Each Academy Wall has an independent once-per-turn trigger")
    void multipleWallsTriggerIndependently() {
        harness.addToBattlefield(player1, new AcademyWall());
        harness.addToBattlefield(player1, new AcademyWall());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }
}
