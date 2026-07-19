package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindbriskHeightsTest extends BaseCardTest {

    /** Puts Windbrisk Heights on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addHeightsWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new WindbriskHeights());
        GameData gd = harness.getGameData();
        Permanent heights = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Windbrisk Heights"))
                .findFirst().orElseThrow();
        gd.setImprintedCard(heights.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return heights;
    }

    @Test
    @DisplayName("Hideaway ETB exiles the chosen card face down and imprints it on the land")
    void hideawayEtbExilesChosenCardFaceDown() {
        GrizzlyBears pick = new GrizzlyBears();
        GrizzlyBears other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(pick, other));
        harness.setHand(player1, List.of(new WindbriskHeights()));
        harness.forceActivePlayer(player1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve the hideaway ETB trigger -> library choice
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent heights = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Windbrisk Heights"))
                .findFirst().orElseThrow();
        ExiledCardEntry entry = gd.findExiledCard(pick.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(heights.getCard())).isSameAs(pick);
        // The other looked-at card went to the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Plays the exiled card when the controller attacked with three or more creatures this turn")
    void playsExiledCardAfterThreeAttackers() {
        GrizzlyBears bears = new GrizzlyBears();
        addHeightsWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing while fewer than three creatures attacked this turn")
    void doesNothingBelowThreshold() {
        GrizzlyBears bears = new GrizzlyBears();
        addHeightsWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability — condition not met

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An opponent's attackers do not satisfy the controller's condition")
    void opponentAttackersDoNotCount() {
        GrizzlyBears bears = new GrizzlyBears();
        addHeightsWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.creaturesAttackedCountThisTurn.put(player2.getId(), 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        addHeightsWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
