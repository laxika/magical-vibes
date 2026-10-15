package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrickstersStratagem.class, GrizzlyBears.class, Island.class})
class TrickstersStratagemTest extends BaseCardTest {

    @Test
    void ownerKeepsCreatureSecondFromTopThenControlledCreatureConnives() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, opponentCreature.getCard(), bottomCard);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void ownerPutsCreatureOnBottomAndConniveTargetMayBeOmitted() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId()));
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, opponentCreature.getCard());
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void requiresOpponentCreatureAsFirstTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresControlledCreatureAsOptionalSecondTarget() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentCreature.getId(), otherOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void omittedConniveTargetWithSingleTargetCastingDoesNotDrawOrDiscard() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawCard = new Island();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, opponentCreature.getId());
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void discardingLandDoesNotAddCounter() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.handleListChoice(player2, "Bottom");
        discardByName("Island");

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureThatIsNoLongerOpponentControlledIsNotMovedButOtherTargetConnives() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card opponentTop = new Island();
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature, ownCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureThatIsNoLongerControlledDoesNotConniveButOpponentCreatureIsMoved() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player2.getId()).add(ownCreature);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ownCreature);
    }

    @Test
    void ownerRatherThanControllerChoosesDestinationAndReceivesCreature() {
        Card ownedCreature = new GrizzlyBears();
        ownedCreature.setOwnerId(player1.getId());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, ownedCreature);
        Card topCard = new Island();
        Card bottomCard = new Island();
        Card opponentTop = new Island();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId()));
        assertThatThrownBy(() -> harness.handleListChoice(player2, "Second from the top"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Second from the top");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, ownedCreature, bottomCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void secondFromTopInEmptyLibraryBecomesOnlyCard() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId()));
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCreature.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void spellDoesNotResolveWhenAllChosenTargetsBecomeIllegal() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawCard = new Island();
        Card opponentTop = new Island();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);
        gd.playerBattlefields.get(player2.getId()).add(ownCreature);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ownCreature);
        harness.assertInGraveyard(player1, "Trickster's Stratagem");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
