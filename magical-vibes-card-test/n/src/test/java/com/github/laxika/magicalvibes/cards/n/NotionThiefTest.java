package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.cards.t.TurnBurn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NotionThief.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class, TurnBurn.class,
        ThoughtReflection.class, Abundance.class})
class NotionThiefTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's extra draw is skipped and the controller draws instead")
    void stealsOpponentExtraDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent's first draw-step draw is not stolen")
    void doesNotStealDrawStepDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("A second draw during the opponent's draw step is stolen")
    void stealsSecondDrawStepDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DRAW);
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("The controller's own draws are unaffected")
    void doesNotAffectControllerDraws() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }
    @Test
    @DisplayName("The controller's own draw replacements apply to the draw Notion Thief gives them")
    void controllersDrawReplacementsApplyToTheStolenDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Opposing Notion Thieves return an extra draw to the original player")
    void opposingThievesReturnDrawToOriginalPlayer() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player2, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A Notion Thief already applied to a draw doesn't apply again after the new drawer's draw choice")
    void appliedThiefIsRememberedAcrossTheNewDrawersChoice() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player1, new Abundance());
        harness.addToBattlefield(player2, new NotionThief());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        for (int i = 0; i < 3 && gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The draw Notion Thief makes instead stays a replaced draw after the new drawer's draw choice")
    void redirectedDrawStaysReplacedAfterTheNewDrawersChoice() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player1, new Abundance());
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Island");
        assertThat(gd.unreplacedDrawnCardIds).doesNotContain(island.getId());
        assertThat(gd.drawEventDepth).isZero();
    }

    @Test
    @DisplayName("A Notion Thief that loses all abilities does not steal draws")
    void abilityLossStopsStealingDraws() {
        var thief = harness.addToBattlefieldAndReturn(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new TurnBurn(), new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, 0, thief.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Thieves controlled by one player steal each draw only once")
    void sameControllerThievesDoNotMultiplyDraws() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("In a three-player game a Thief of the opponent in the third seat steals the extra draw")
    void stealsDrawForThirdSeatOpponent() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player3, new NotionThief());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player3, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player3.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

    @Test
    @DisplayName("With Notion Thieves under two opponents the drawing player chooses which applies; the other then applies to the new draw")
    void drawingPlayerChoosesAmongOpponentsThieves() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new NotionThief());
        harness.addToBattlefield(player3, new NotionThief());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player3, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        chooseReplacementControlledBy(player1, player3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player3.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing the first-seat opponent's Notion Thief lets the other opponent's Thief take the draw")
    void choosingFirstSeatThiefHandsDrawToOtherOpponent() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new NotionThief());
        harness.addToBattlefield(player3, new NotionThief());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player3, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        chooseReplacementControlledBy(player1, player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player3.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    private void chooseReplacementControlledBy(Player chooser, Player controller) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(chooser.getId());
        String controllerName = gd.playerIdToName.get(controller.getId());
        harness.handleListChoice(chooser, choice.options().stream()
                .filter(option -> option.contains(controllerName)).findFirst().orElseThrow());
    }
}
