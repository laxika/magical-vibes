package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WrennAndSevenTest extends BaseCardTest {

    @Nested
    @DisplayName("+1 ability")
    class PlusOne {

        @Test
        @DisplayName("Reveals top four: lands to hand, rest to graveyard")
        void landsToHandRestToGraveyard() {
            Permanent wrenn = addReadyWrenn(player1);
            Card forest = new Forest();
            Card island = new Island();
            Card bears = new GrizzlyBears();
            Card shock = new Shock();
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(forest, island, bears, shock));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
            assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Shock");
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Nested
    @DisplayName("0 ability")
    class Zero {

        @Test
        @DisplayName("Puts chosen lands from hand onto the battlefield tapped until declined")
        void putsLandsTappedUntilDeclined() {
            Permanent wrenn = addReadyWrenn(player1);
            Card forest = new Forest();
            Card island = new Island();
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(forest, island, bears));

            assertThat(forest.hasType(CardType.LAND)).isTrue();
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island, bears);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
            var choice = (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
            assertThat(choice.validIndices()).contains(0, 1);
            assertThat(choice.putAnyNumber()).isTrue();

            harness.handleCardChosen(player1, 0); // Forest
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Forest") && p.isTapped());
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

            harness.handleCardChosen(player1, -1);

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).contains(island, bears);
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().hasType(CardType.LAND))
                    .count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Declining immediately puts no lands")
        void decliningPutsNone() {
            addReadyWrenn(player1);
            Card forest = new Forest();
            harness.setHand(player1, List.of(forest));

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, -1);

            assertThat(gd.playerHands.get(player1.getId())).contains(forest);
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .noneMatch(p -> p.getCard().getName().equals("Forest"))).isTrue();
        }
    }

    @Nested
    @DisplayName("−3 ability")
    class MinusThree {

        @Test
        @DisplayName("Creates a Treefolk with reach whose P/T equal controlled lands")
        void createsTreefolkScaledToLands() {
            Permanent wrenn = addReadyWrenn(player1);
            wrenn.setCounterCount(CounterType.LOYALTY, 5);
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Island());
            harness.addToBattlefield(player2, new Forest());

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            Permanent treefolk = findPermanent(player1, "Treefolk");
            assertThat(treefolk.hasKeyword(Keyword.REACH)).isTrue();
            // 3 lands you control (Wrenn is not a land)
            assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("−8 ability")
    class MinusEight {

        @Test
        @DisplayName("Returns all permanent cards from graveyard to hand and grants no max hand size")
        void returnsPermanentsAndGrantsNoMaxHandSize() {
            Permanent wrenn = addReadyWrenn(player1);
            wrenn.setCounterCount(CounterType.LOYALTY, 8);
            Card forest = new Forest();
            Card bears = new GrizzlyBears();
            Card shock = new Shock();
            gd.playerGraveyards.get(player1.getId()).addAll(List.of(forest, bears, shock));

            harness.activateAbility(player1, 0, 3, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
            assertThat(gd.playerHands.get(player1.getId())).contains(forest, bears);
            harness.assertInGraveyard(player1, "Shock");
            assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        }
    }

    private Permanent addReadyWrenn(Player player) {
        WrennAndSeven card = new WrennAndSeven();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
