package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatGildedBoat.class, GrizzlyBears.class, Mountain.class})
class GreatGildedBoatTest extends BaseCardTest {

    @Test
    void attackingAfterCrewRecruitsWhenDiscardingNonland() {
        Permanent boat = addCrewedBoat();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Human Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldiers.getFirst().getCard().getSubtypes())
                .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, boat)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void attackingAfterCrewDoesNotRecruitWhenDiscardingLand() {
        addCrewedBoat();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    void uncrewedBoatRecruitsOnlyOnceForMultipleAttackers() {
        harness.addToBattlefield(player1, new GreatGildedBoat());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        discardByName("Grizzly Bears");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Great Gilded Boat"))).isFalse();
    }

    @Test
    void opponentsAttackDoesNotRecruit() {
        harness.addToBattlefield(player1, new GreatGildedBoat());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void recruitCreatesTokenBeforePlayersCanRespondAfterDiscard() {
        harness.addToBattlefield(player1, new GreatGildedBoat());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.DiscardChoice.class);
            discardByName("Grizzly Bears");

            assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
        });
    }

    private Permanent addCrewedBoat() {
        Permanent boat = addCreatureReady(player1, new GreatGildedBoat());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return boat;
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
