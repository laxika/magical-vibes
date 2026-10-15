package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WrennAndOne.class, GrizzlyBears.class, Confiscate.class})
class WrennAndOneTest extends BaseCardTest {

    @Test
    @DisplayName("Playing Wrenn uses a land play, bypasses the stack, and gives one loyalty")
    void entersAsLandPlaneswalker() {
        WrennAndOne card = new WrennAndOne();
        harness.setHand(player1, java.util.List.of(card));
        harness.playLand(player1, 0);

        Permanent wrenn = findPermanent(player1, "Wrenn and One");
        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, java.util.List.of(new WrennAndOne()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Nested
    @DisplayName("Loyalty abilities")
    @CardUsed({WrennAndOne.class, GrizzlyBears.class, Confiscate.class})
    class LoyaltyAbilities {

        @Test
        void plusOneGrantsTemporaryManaAbility() {
            Permanent wrenn = addReadyWrenn(player1, 1);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            harness.activateAbility(player1, 0, 3, null, null);
            assertThat(wrenn.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void minusOneCreatesSquirrel() {
            Permanent wrenn = addReadyWrenn(player1, 1);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
            harness.assertNotOnBattlefield(player1, "Wrenn and One");
            harness.assertInGraveyard(player1, "Wrenn and One");
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard().getName().equals("Squirrel")
                            && permanent.getCard().getPower() == 1
                            && permanent.getCard().getToughness() == 1
                            && permanent.getCard().getColor() == CardColor.GREEN
                            && permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL));
        }

        @Test
        void minusFourEmblemAddsManaForControlledCreatures() {
            addReadyWrenn(player1, 4);
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(gd.emblems).hasSize(1);
            Emblem emblem = gd.emblems.getFirst();
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());

            harness.forceStep(TurnStep.DRAW);
            harness.passUntil(TurnStep.PRECOMBAT_MAIN);
            harness.passBothPriorities();

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        }

        @Test
        void grantedManaAbilityWorksOnTheTurnWrennIsPlayed() {
            harness.setHand(player1, java.util.List.of(new WrennAndOne()));
            harness.playLand(player1, 0);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            harness.activateAbility(player1, 0, 3, null, null);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void grantedManaAbilitySurvivesOpponentsTurnButExpiresOnYourNextTurn() {
            addReadyWrenn(player1, 1);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player2, TurnStep.UPKEEP);

            harness.activateAbility(player1, 0, 3, null, null);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player1, TurnStep.UPKEEP);
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        }

        @Test
        @CardUsed({WrennAndOne.class, Confiscate.class})
        void grantedManaAbilityExpiresOnActivatingPlayersNextTurnAfterControlChanges() {
            Permanent wrenn = addReadyWrenn(player1, 1);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player2, TurnStep.UPKEEP);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player2, java.util.List.of(new Confiscate()));
            harness.addMana(player2, ManaColor.BLUE, 6);
            harness.castEnchantment(player2, 0, wrenn.getId());
            harness.passBothPriorities();
            harness.assertOnBattlefield(player2, "Wrenn and One");

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player1, TurnStep.UPKEEP);
            int wrennIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wrenn);

            assertThatThrownBy(() -> harness.activateAbility(player2, wrennIndex, 3, null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        }

        @Test
        void cannotActivateAnotherLoyaltyAbilityAfterPlusOne() {
            addReadyWrenn(player1, 4);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotCreateEmblemWithoutEnoughLoyalty() {
            addReadyWrenn(player1, 3);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.emblems).isEmpty();
        }

        @Test
        void emblemCountsCreaturesAtResolutionAndIgnoresOpponentsCreatures() {
            addReadyWrenn(player1, 4);
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Wrenn and One");
            harness.forceStep(TurnStep.DRAW);
            harness.passUntil(TurnStep.PRECOMBAT_MAIN);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.passBothPriorities();

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        }

        @Test
        void emblemDoesNotTriggerOnOpponentsPrecombatMainPhase() {
            addReadyWrenn(player1, 4);
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DRAW);

            harness.passUntil(TurnStep.PRECOMBAT_MAIN);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        }

        @Test
        void emblemAddsNoManaWithoutCreatures() {
            addReadyWrenn(player1, 4);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.DRAW);
            harness.passUntil(TurnStep.PRECOMBAT_MAIN);
            harness.passBothPriorities();

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        }

        @Test
        void emblemDoesNotTriggerOnPostcombatMainPhase() {
            addReadyWrenn(player1, 4);
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.END_OF_COMBAT);

            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        }
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WrennAndOne());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
