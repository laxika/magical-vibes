package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({OrderOfSuccession.class, BalefulStrix.class})
class OrderOfSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("each player chooses from the next player and gains their chosen creature")
    void eachPlayerChoosesNextPlayersCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        harness.addToBattlefield(player2, new BalefulStrix());
        castOrderOfSuccession(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(chosenCreature.getId());

        harness.handlePermanentChosen(player1, chosenCreature.getId());

        assertThat(controls(player1, chosenCreature)).isTrue();
        assertThat(controls(player2, ownCreature)).isTrue();
    }

    @Test
    @DisplayName("skips a player with no creatures")
    void skipsPlayerWithNoCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        castOrderOfSuccession(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(controls(player2, ownCreature)).isTrue();
    }

    @Test
    @DisplayName("the direction is chosen during resolution rather than casting")
    void choosesDirectionDuringResolution() {
        harness.addToBattlefield(player1, new BalefulStrix());
        harness.addToBattlefield(player2, new BalefulStrix());
        harness.setHand(player1, List.of(new OrderOfSuccession()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Right");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("leftward choices proceed leftward and control changes wait for every choice")
    void leftwardChoicesProceedInChosenDirection() {
        Player thirdPlayer = addThirdPlayer();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        harness.addToBattlefield(player1, new BalefulStrix());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        harness.addToBattlefield(player2, new BalefulStrix());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(thirdPlayer, new BalefulStrix());
        harness.addToBattlefield(thirdPlayer, new BalefulStrix());
        harness.setHand(player1, List.of(new OrderOfSuccession()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) == null) {
            harness.handleListChoice(player1, "Left");
        }

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).contains(thirdCreature.getId()).doesNotContain(secondCreature.getId());
        harness.handlePermanentChosen(player1, thirdCreature.getId());

        assertThat(controls(thirdPlayer, thirdCreature)).isTrue();
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(thirdPlayer.getId());
        assertThat(secondChoice.validIds()).contains(secondCreature.getId());
        harness.handlePermanentChosen(thirdPlayer, secondCreature.getId());

        assertThat(controls(player2, secondCreature)).isTrue();
        PendingInteraction.PermanentChoice finalChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(finalChoice).isNotNull();
        assertThat(finalChoice.playerId()).isEqualTo(player2.getId());
        assertThat(finalChoice.validIds()).contains(firstCreature.getId());
        harness.handlePermanentChosen(player2, firstCreature.getId());

        assertThat(controls(player1, thirdCreature)).isTrue();
        assertThat(controls(thirdPlayer, secondCreature)).isTrue();
        assertThat(controls(player2, firstCreature)).isTrue();
    }

    @Test
    @DisplayName("nothing happens when neither player controls a creature")
    void noCreaturesLeavesNoPendingChoice() {
        castOrderOfSuccession(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player thirdPlayer = new Player(thirdPlayerId, "Charlie");
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
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return thirdPlayer;
    }

    private void castOrderOfSuccession(Player player, int directionMode) {
        harness.setHand(player, List.of(new OrderOfSuccession()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player, 0, directionMode);
        if (gd.interaction.activeInteraction() != null
                && gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) == null) {
            harness.handleListChoice(player, directionMode == 0 ? "Left" : "Right");
        }
    }

    private boolean controls(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).contains(permanent);
    }
}
