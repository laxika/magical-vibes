package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TamiyoTheMoonSageTest extends BaseCardTest {

    @Nested
    @DisplayName("+1: Tap target permanent")
    class PlusOne {

        @Test
        @DisplayName("Taps the target and marks it to skip its next untap step")
        void tapsAndLocksTarget() {
            addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");
            UUID bearsId = bears.getId();

            harness.activateAbility(player1, 0, 0, null, bearsId);
            harness.passBothPriorities();

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Can tap a noncreature permanent — any permanent is a legal target")
        void tapsLand() {
            addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player2, new Plains());
            Permanent plains = findPermanent(player2, "Plains");

            harness.activateAbility(player1, 0, 0, null, plains.getId());
            harness.passBothPriorities();

            assertThat(plains.isTapped()).isTrue();
            assertThat(plains.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Adds a loyalty counter")
        void addsLoyalty() {
            Permanent tamiyo = addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.activateAbility(player1, 0, 0, null, findPermanent(player2, "Grizzly Bears").getId());
            harness.passBothPriorities();

            assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("-2: Draw for each tapped creature target player controls")
    class MinusTwo {

        @Test
        @DisplayName("Draws one card per tapped creature the targeted player controls")
        void drawsPerTappedCreature() {
            Permanent tamiyo = addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            List<Permanent> bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .toList();
            bears.get(0).tap();
            bears.get(1).tap();

            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
            assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        }

        @Test
        @DisplayName("Untapped creatures are not counted")
        void drawsNothingWhenNoTappedCreatures() {
            addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player2, new GrizzlyBears());

            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        }

        @Test
        @DisplayName("Can target its own controller — counts only that player's tapped creatures")
        void canTargetSelf() {
            addReadyTamiyo(player1, 4);
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            findPermanent(player1, "Grizzly Bears").tap();
            findPermanent(player2, "Grizzly Bears").tap();

            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.activateAbility(player1, 0, 1, null, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        }
    }

    @Nested
    @DisplayName("-8: Emblem")
    class MinusEight {

        @Test
        @DisplayName("Grants no maximum hand size and creates the graveyard-return emblem")
        void createsEmblem() {
            addReadyTamiyo(player1, 8);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
            assertThat(gd.emblems).hasSize(1);
            assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Emblem lets its controller return a card put into their graveyard to hand")
        void emblemReturnsCardToHand() {
            giveEmblem(player1);
            harness.setHand(player1, List.of(new AngelsMercy()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            // Angel's Mercy resolved and hit the graveyard, putting the emblem trigger on the stack.
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Angel's Mercy"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Angel's Mercy"));
        }

        @Test
        @DisplayName("Declining the emblem's optional return leaves the card in the graveyard")
        void emblemReturnIsOptional() {
            giveEmblem(player1);
            harness.setHand(player1, List.of(new AngelsMercy()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Angel's Mercy"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Angel's Mercy"));
        }

        @Test
        @DisplayName("Emblem does not trigger on an opponent's graveyard")
        void emblemIgnoresOpponentGraveyard() {
            giveEmblem(player1);
            harness.setHand(player2, List.of(new AngelsMercy()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.castInstant(player2, 0);
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Angel's Mercy"));
        }
    }

    private void giveEmblem(Player player) {
        Permanent tamiyo = addReadyTamiyo(player, 8);
        harness.activateAbility(player, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);
        assertThat(tamiyo).isNotNull();
    }

    private Permanent addReadyTamiyo(Player player, int loyalty) {
        Permanent perm = new Permanent(new TamiyoTheMoonSage());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
