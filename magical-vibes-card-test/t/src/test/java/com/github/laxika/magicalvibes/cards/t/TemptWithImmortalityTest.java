package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({TemptWithImmortality.class, GrizzlyBears.class})
class TemptWithImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature for the controller and an accepting opponent, then rewards the controller")
    void acceptingOpponentReturnsCreaturesForBothPlayersAndRewardsController() {
        Card ownFirst = new GrizzlyBears();
        Card ownReward = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownFirst, ownReward));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        // Each player then has one creature card to return, so it returns unasked (CR 608.2d).
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(ownFirst.getId(), ownReward.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional creature")
    void decliningOpponentDoesNotReturnAnAdditionalCreature() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("An empty controller graveyard does not prevent an opponent from accepting")
    void emptyControllerGraveyardStillAllowsOpponentReturn() {
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("An opponent without creatures cannot grant an additional return")
    void opponentWithoutCreaturesDoesNotGrantReward() {
        Card ownFirst = new GrizzlyBears();
        Card ownReward = new GrizzlyBears();
        Card noncreature = new TemptWithImmortality();
        harness.setGraveyard(player1, List.of(ownFirst, ownReward));
        harness.setGraveyard(player2, List.of(noncreature));

        Card spell = castTemptWithImmortality();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownFirst.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(ownReward, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Creature choices skip noncreature cards in both graveyards")
    void returnsOnlyCreaturesFromMixedGraveyards() {
        Card ownNoncreature = new TemptWithImmortality();
        Card opponentNoncreature = new TemptWithImmortality();
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownNoncreature, ownCreature));
        harness.setGraveyard(player2, List.of(opponentNoncreature, opponentCreature));

        Card spell = castTemptWithImmortality();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(ownNoncreature, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentNoncreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("No opponent returns a creature until every opponent has decided; then they return together")
    void opponentsDecideBeforeAnyCreatureReturns() {
        Player player3 = addThirdPlayer();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(second));
        harness.setGraveyard(player3, List.of(third));
        harness.setHand(player1, List.of(new TemptWithImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0);
        passUntilAwaitingInput(player3);

        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MayAbilityChoice offer = gd.interaction.activeInteraction(
                PendingInteraction.MayAbilityChoice.class);
        assertThat(offer).isNotNull();
        assertThat(offer.playerId()).isEqualTo(player3.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.handleMayAbilityChosen(player3, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getId());
        assertThat(gd.playerBattlefields.get(player3.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(third.getId());
    }

    @Test
    @DisplayName("Only an opponent with a creature card in their graveyard is offered the return")
    void opponentWithoutCreatureCardIsNotAsked() {
        Player player3 = addThirdPlayer();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(new TemptWithImmortality()));
        harness.setGraveyard(player3, List.of(third));
        harness.setHand(player1, List.of(new TemptWithImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0);
        passUntilAwaitingInput(player3);

        PendingInteraction.MayAbilityChoice offer = gd.interaction.activeInteraction(
                PendingInteraction.MayAbilityChoice.class);
        assertThat(offer).isNotNull();
        assertThat(offer.playerId()).isEqualTo(player3.getId());
        harness.handleMayAbilityChosen(player3, true);

        assertThat(gd.playerBattlefields.get(player3.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(third.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void passUntilAwaitingInput(Player player3) {
        for (int i = 0; i < 20 && !gd.interaction.isAwaitingInput(); i++) {
            UUID priorityId = gqs.getPriorityPlayerId(gd);
            Player priorityPlayer = List.of(player1, player2, player3).stream()
                    .filter(player -> player.getId().equals(priorityId)).findFirst().orElseThrow();
            harness.passPriority(priorityPlayer);
        }
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }

    /** Casts and resolves the spell, returning its card (which ends up in its owner's graveyard). */
    private Card castTemptWithImmortality() {
        Card spell = new TemptWithImmortality();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        return spell;
    }
}
