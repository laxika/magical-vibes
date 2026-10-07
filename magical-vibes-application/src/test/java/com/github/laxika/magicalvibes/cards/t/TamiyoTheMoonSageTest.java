package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Dreadwaters;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Recycle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TamiyoTheMoonSage.class, AngelsMercy.class, GrizzlyBears.class, Plains.class})
class TamiyoTheMoonSageTest extends BaseCardTest {

    @Nested
    @DisplayName("+1: Tap target permanent")
    @CardUsed({TamiyoTheMoonSage.class, GrizzlyBears.class, Plains.class})
    class PlusOne {

        @Test
        void skipsOnlyTargetsControllersNextUntapStep() {
            addReadyTamiyo(player1, 4);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            harness.activateAbility(player1, 0, 0, null, bears.getId());
            harness.passBothPriorities();

            harness.performUntapStep(player1);
            harness.performUntapStep(player2);
            assertThat(bears.isTapped()).isTrue();

            harness.performUntapStep(player2);
            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        void alreadyTappedTargetStillSkipsNextUntap() {
            addReadyTamiyo(player1, 4);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            bears.tap();

            harness.activateAbility(player1, 0, 0, null, bears.getId());
            harness.passBothPriorities();
            harness.performUntapStep(player2);

            assertThat(bears.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Taps the target and marks it to skip its next untap step")
        void tapsAndLocksTarget() {
            addReadyTamiyo(player1, 4);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
            Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

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
    @CardUsed({TamiyoTheMoonSage.class, GrizzlyBears.class, Plains.class})
    class MinusTwo {

        @Test
        void countsTappedCreaturesAtResolutionAndExcludesTappedLands() {
            addReadyTamiyo(player1, 4);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
            plains.tap();
            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            bears.tap();
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        }

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
    @CardUsed({TamiyoTheMoonSage.class, AngelsMercy.class, Plains.class})
    class MinusEight {

        @Test
        void emblemAllowsKeepingMoreThanSevenCardsThroughCleanup() {
            giveEmblem(player1);
            harness.setHand(player1, List.of(new Plains(), new Plains(), new Plains(),
                    new Plains(), new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));
            harness.forceStep(TurnStep.END_STEP);

            harness.passUntil(player2, TurnStep.UPKEEP);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        }

        @Test
        @CardUsed(Recycle.class)
        void laterHandSizeSettingEffectOverridesEmblem() {
            giveEmblem(player1);
            harness.castFromHand(player1, new Recycle(), "{4}{G}{G}");
            harness.passBothPriorities();
            harness.setHand(player1, List.of(new Plains(), new Plains(), new Plains()));
            harness.forceStep(TurnStep.END_STEP);

            gs.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                    .isEqualTo(1);
        }

        @Test
        void tamiyoDiesBeforeEmblemExistsWhenActivatedWithEightLoyalty() {
            addReadyTamiyo(player1, 8);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.assertInGraveyard(player1, "Tamiyo, the Moon Sage");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Tamiyo, the Moon Sage");
            harness.assertNotInHand(player1, "Tamiyo, the Moon Sage");
        }

        @Test
        @CardUsed(Dreadwaters.class)
        void emblemReturnsCardMilledFromLibrary() {
            giveEmblem(player1);
            harness.addToBattlefield(player1, new Plains());
            AngelsMercy milledCard = new AngelsMercy();
            harness.setLibrary(player1, List.of(milledCard, new Plains()));
            harness.setHand(player1, List.of(new Dreadwaters()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            harness.castSorcery(player1, 0, player1.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerHands.get(player1.getId())).contains(milledCard);
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(milledCard);
        }

        @Test
        void olderEmblemTriggerCannotReturnCardThatLeftAndReenteredGraveyard() {
            giveEmblem(player1);
            addReadyTamiyo(player1, 9);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            assertThat(gd.emblems).hasSize(2);

            AngelsMercy mercy = new AngelsMercy();
            harness.setHand(player1, List.of(mercy));
            harness.addMana(player1, ManaColor.WHITE, 8);
            harness.castInstant(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerHands.get(player1.getId())).contains(mercy);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerGraveyards.get(player1.getId())).contains(mercy);
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mercy);
        }

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
        addReadyTamiyo(player, 8);
        harness.activateAbility(player, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);
    }

    private Permanent addReadyTamiyo(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TamiyoTheMoonSage());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
