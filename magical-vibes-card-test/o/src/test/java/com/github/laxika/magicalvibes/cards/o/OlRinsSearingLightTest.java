package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OlRinsSearingLight.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class OlRinsSearingLightTest extends BaseCardTest {

    @Test
    @DisplayName("Without spell mastery, only the greatest-power creature is exiled")
    void exilesOnlyTheGreatestPowerCreatureWithoutSpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castSearingLight();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(angel.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With spell mastery, it deals damage equal to the exiled creature's power")
    void spellMasteryDealsDamageEqualToTheExiledCreaturePower() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        castSearingLight();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(angel.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The opponent chooses among creatures tied for greatest power")
    void opponentChoosesAmongTiedGreatestPowerCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        castSearingLight();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Each opponent chooses in turn order, then the chosen creatures are exiled at once (ruling)")
    void opponentsChooseThenCreaturesAreExiledTogether() {
        Player player3 = addThirdPlayer();
        Permanent firstChoice = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent secondChoice = harness.addToBattlefieldAndReturn(player3, new SerraAngel());
        harness.addToBattlefield(player3, new SerraAngel());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        castSearingLight();

        harness.handlePermanentChosen(player2, firstChoice.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstChoice);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player3.getId());

        harness.handlePermanentChosen(player3, secondChoice.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstChoice.getCard());
        assertThat(gd.getPlayerExiledCards(player3.getId())).containsExactly(secondChoice.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player3.getId())).isEqualTo(16);
    }

    private Player addThirdPlayer() {
        Player player3 = new Player(UUID.randomUUID(), "Third opponent");
        gd.playerIds.add(player3.getId());
        gd.orderedPlayerIds.add(player3.getId());
        gd.playerNames.add(player3.getUsername());
        gd.playerIdToName.put(player3.getId(), player3.getUsername());
        gd.playerDecks.put(player3.getId(), new ArrayList<>());
        gd.playerHands.put(player3.getId(), new ArrayList<>());
        gd.playerGraveyards.put(player3.getId(), new ArrayList<>());
        gd.playerBattlefields.put(player3.getId(), new ArrayList<>());
        gd.playerManaPools.put(player3.getId(), new ManaPool());
        gd.playerLifeTotals.put(player3.getId(), 20);
        return player3;
    }

    private void castSearingLight() {
        harness.setHand(player1, List.of(new OlRinsSearingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
