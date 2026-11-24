package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.CrimsonAcolyte;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileStep;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathOrGlory.class, ArdentSoldier.class, CrimsonAcolyte.class, DrakeSkullCameo.class})
class DeathOrGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Controller separates creature cards and opponent chooses the pile to exile")
    void opponentChoosesPileToExile() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(soldier, acolyte, artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validCardIds()).containsExactlyInAnyOrder(soldier.getId(), acolyte.getId());

        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));

        PendingInteraction.MayAbilityChoice pileChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(pileChoice).isNotNull();
        assertThat(pileChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(soldier);
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
        assertThat(gd.hasPendingInteraction(PendingPileStep.class)).isFalse();
    }

    @Test
    @DisplayName("Choosing the second pile to exile returns the first pile")
    void opponentChoosesSecondPileToExile() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        harness.setGraveyard(player1, List.of(soldier, acolyte));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(acolyte);
    }

    @Test
    @DisplayName("An empty pile is legal and returning the other pile puts all creatures onto the battlefield")
    void emptyPileCanBeChosen() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(soldier, acolyte, artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        harness.assertOnBattlefield(player1, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
        assertThat(gd.hasPendingInteraction(PendingPileStep.class)).isFalse();
    }

    @Test
    @DisplayName("A graveyard with no creature cards does not create a pile choice")
    void noCreatureCardsDoNothing() {
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.hasPendingInteraction(PendingPileStep.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
    }

    @Test
    @DisplayName("The opponent can exile all creatures by choosing the nonempty pile")
    void allCreaturesCanBeExiled() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        harness.setGraveyard(player1, List.of(soldier, acolyte));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId(), acolyte.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(soldier, acolyte);
        harness.assertNotOnBattlefield(player1, "Ardent Soldier");
        harness.assertNotOnBattlefield(player1, "Crimson Acolyte");
        harness.assertNotInGraveyard(player1, "Ardent Soldier");
        harness.assertNotInGraveyard(player1, "Crimson Acolyte");
        harness.assertInGraveyard(player1, "Death or Glory");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.hasPendingInteraction(PendingPileStep.class)).isFalse();
    }

    @Test
    @DisplayName("A single creature returns without affecting the opponent's graveyard")
    void onlyControllersGraveyardIsSeparated() {
        Card soldier = new ArdentSoldier();
        Card opposingAcolyte = new CrimsonAcolyte();
        harness.setGraveyard(player1, List.of(soldier));
        harness.setGraveyard(player2, List.of(opposingAcolyte));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation.validCardIds()).containsExactly(soldier.getId());
        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        harness.assertNotInGraveyard(player1, "Ardent Soldier");
        harness.assertInGraveyard(player2, "Crimson Acolyte");
        harness.assertNotOnBattlefield(player1, "Crimson Acolyte");
        harness.assertNotOnBattlefield(player2, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.hasPendingInteraction(PendingPileStep.class)).isFalse();
    }

    @Test
    @DisplayName("With several opponents, the controller chooses which opponent chooses the pile to exile")
    void controllerChoosesTheOpponentWhoChooses() {
        Player player3 = addOpponent();
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        harness.setGraveyard(player1, List.of(soldier, acolyte));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());

        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.handleMayAbilityChosen(player3, true);

        harness.assertOnBattlefield(player1, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(soldier);
    }

    private Player addOpponent() {
        UUID playerId = UUID.randomUUID();
        Player opponent = new Player(playerId, "Charlie");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(playerId, "Charlie");
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), playerId, "Charlie");
        return opponent;
    }
}
