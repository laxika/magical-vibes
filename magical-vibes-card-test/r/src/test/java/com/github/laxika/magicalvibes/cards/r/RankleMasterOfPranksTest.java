package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RankleMasterOfPranks.class, RovingKeep.class})
class RankleMasterOfPranksTest extends BaseCardTest {

    private static final String NO_MODES = "Choose no modes";
    private static final String ALL_MODES = "Each player discards a card; each player loses 1 life and draws a card; "
            + "each player sacrifices a creature";

    @Test
    @DisplayName("The combat-damage trigger may choose no modes")
    void choosesNoModes() {
        Permanent rankle = addReadyRankle();
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.setHand(player1, List.of(new RovingKeep()));
        harness.setHand(player2, List.of(new RovingKeep()));
        rankle.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, NO_MODES);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing all modes resolves them in printed order for every player")
    void choosesAllModes() {
        Permanent rankle = addReadyRankle();
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.setHand(player1, List.of(new RovingKeep(), new RovingKeep()));
        harness.setHand(player2, List.of(new RovingKeep(), new RovingKeep()));
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setLibrary(player2, List.of(new RovingKeep()));
        rankle.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, ALL_MODES);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Creature.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Rankle, Master of Pranks");
        harness.assertInGraveyard(player1, "Roving Keep");
        harness.assertInGraveyard(player2, "Roving Keep");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Modes are announced before players can respond to the combat-damage trigger")
    void announcesModesWhenTriggerGoesOnStack() {
        Permanent rankle = addReadyRankle();
        rankle.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, NO_MODES);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "true,false,false",
            "false,true,false",
            "false,false,true",
            "true,true,false",
            "true,false,true",
            "false,true,true",
            "true,true,true"
    })
    @DisplayName("Every nonempty subset applies exactly its selected modes")
    void resolvesSelectedModes(boolean discard, boolean draw, boolean sacrifice) {
        Permanent rankle = addReadyRankle();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.addToBattlefield(player2, new RovingKeep());
        harness.setHand(player1, List.of(new RovingKeep(), new RovingKeep()));
        harness.setHand(player2, List.of(new RovingKeep(), new RovingKeep()));
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setLibrary(player2, List.of(new RovingKeep()));
        rankle.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        List<String> modes = new ArrayList<>();
        if (discard) modes.add("Each player discards a card");
        if (draw) modes.add("each player loses 1 life and draws a card");
        if (sacrifice) modes.add("each player sacrifices a creature");
        String choice = String.join("; ", modes);
        choice = Character.toUpperCase(choice.charAt(0)) + choice.substring(1);
        harness.handleListChoice(player1, choice);
        if (discard) {
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player2, 0);
        }
        if (sacrifice) {
            harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));
            harness.handleMultiplePermanentsChosen(player2, List.of(opposingCreature.getId()));
        }
        assertThat(gd.getLife(player1.getId())).isEqualTo(draw ? 19 : 20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(draw ? 16 : 17);
        int handSize = 2 - (discard ? 1 : 0) + (draw ? 1 : 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerBattlefields.get(player1.getId()).contains(ownCreature)).isEqualTo(!sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId()).contains(opposingCreature)).isEqualTo(!sacrifice);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discard choices are collected before any selected card is discarded")
    void discardsSimultaneouslyAfterBothPlayersChoose() {
        Permanent rankle = addReadyRankle();
        RovingKeep ownCard = new RovingKeep();
        RovingKeep opposingCard = new RovingKeep();
        harness.setHand(player1, List.of(ownCard));
        harness.setHand(player2, List.of(opposingCard));
        rankle.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Each player discards a card");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCard);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
    }

    @Test
    @DisplayName("Empty hands do not prevent later draw and sacrifice modes")
    void emptyHandsStillDrawAndSacrificeRankle() {
        Permanent rankle = addReadyRankle();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setLibrary(player2, List.of(new RovingKeep()));
        rankle.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ALL_MODES);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Rankle, Master of Pranks");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyRankle() {
        return addCreatureReady(player1, new RankleMasterOfPranks());
    }

}
