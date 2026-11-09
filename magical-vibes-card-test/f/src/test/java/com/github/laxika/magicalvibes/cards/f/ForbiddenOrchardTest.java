package com.github.laxika.magicalvibes.cards.f;

import java.util.UUID;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ForbiddenOrchard.class})
class ForbiddenOrchardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds the chosen color and gives the opponent a Spirit token")
    void tapForManaGivesOpponentSpirit() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForbiddenOrchard());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(land.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent spirit = findPermanents(player2, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors()).isEmpty();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().isToken()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("A second activation on a later turn makes a second Spirit for the opponent")
    void eachTapMakesAnotherSpirit() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        advanceToNextTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Only the Orchard that was tapped creates the opponent's Spirit token")
    void onlyTappedOrchardTriggers() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("No Spirit is created while the land is untapped")
    void noSpiritWithoutTapping() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Mana is available immediately but the Spirit waits for the targeted trigger to resolve")
    void spiritCreationUsesTheStack() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Orchard gives its Spirit to the other player")
    void opponentOrchardGivesSpiritToUs() {
        harness.addToBattlefield(player2, new ForbiddenOrchard());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    private void advanceToNextTurn() {
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
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
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

    @Test
    @DisplayName("The controller chooses which opponent creates the Spirit; the controller is not a legal target")
    void controllerChoosesWhichOpponentCreatesTheSpirit() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player3, "Spirit")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
        harness.assertNotOnBattlefield(player1, "Spirit");
    }
}
