package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MultaniYavimayasAvatar.class, Forest.class, Plains.class, GrizzlyBears.class, GloriousAnthem.class, MerfolkLooter.class})
class MultaniYavimayasAvatarTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Power/Toughness boost")
    @CardUsed({MultaniYavimayasAvatar.class, Forest.class, Plains.class, GrizzlyBears.class, GloriousAnthem.class})
    class PowerToughnessTests {

        @Test
        @DisplayName("P/T equals lands you control when no lands in graveyard")
        void ptEqualsControlledLands() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Plains());

            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
        }

        @Test
        @DisplayName("P/T includes land cards in graveyard")
        void ptIncludesGraveyardLands() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.setGraveyard(player1, List.of(new Forest(), new Plains()));

            // 1 land on battlefield + 2 lands in graveyard = 3
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
        }

        @Test
        @DisplayName("P/T counts both battlefield and graveyard lands")
        void ptCountsBothZones() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.setGraveyard(player1, List.of(new Forest(), new Plains(), new Forest()));

            // 2 battlefield + 3 graveyard = 5
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(5);
        }

        @Test
        @DisplayName("Does not count opponent lands on battlefield or in graveyard")
        void doesNotCountOpponentLands() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player2, new Forest());
            harness.addToBattlefield(player2, new Plains());
            harness.setGraveyard(player2, List.of(new Forest()));

            // Only player1's 1 land counts
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not count non-land cards in graveyard")
        void doesNotCountNonLandCardsInGraveyard() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));

            // 1 battlefield land + 1 graveyard land (GrizzlyBears doesn't count) = 2
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(2);
        }

        @Test
        @DisplayName("Dies with no lands anywhere")
        void diesWithNoLands() {
            addCreatureReady(player1, new MultaniYavimayasAvatar());

            // 0/0 with no boost = dies to state-based actions
            harness.runStateBasedActions();

            harness.assertNotOnBattlefield(player1, "Multani, Yavimaya's Avatar");
            harness.assertInGraveyard(player1, "Multani, Yavimaya's Avatar");
        }

        @Test
        @DisplayName("P/T stacks with other static bonuses")
        void ptStacksWithOtherStaticBonuses() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new GloriousAnthem());

            // 2 lands + 1/1 from Glorious Anthem = 3/3
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
        }

        @Test
        @DisplayName("P/T updates dynamically when lands change")
        void ptUpdatesDynamically() {
            Permanent multani = addCreatureReady(player1, new MultaniYavimayasAvatar());
            harness.addToBattlefield(player1, new Forest());

            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);

            harness.addToBattlefield(player1, new Plains());
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(2);

            // Remove all lands from battlefield
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
            assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(0);
        }

    }

    @Nested
    @DisplayName("Graveyard activated ability")
    @CardUsed({MultaniYavimayasAvatar.class, Forest.class, Plains.class, MerfolkLooter.class})
    class GraveyardAbilityTests {

        @Test
        @DisplayName("Returns only the Multani whose ability was activated")
        void returnsOnlySourceCopy() {
            MultaniYavimayasAvatar source = new MultaniYavimayasAvatar();
            MultaniYavimayasAvatar other = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(source, other));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).contains(source).doesNotContain(other);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        }

        @Test
        @DisplayName("Tapped lands can pay the return cost")
        void canReturnTappedLands() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
            harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).contains(multani);
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("A borrowed land pays the cost but returns to its owner's hand")
        void returnedLandGoesToItsOwner() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            Forest borrowedLand = new Forest();
            Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedLand);
            gd.stolenCreatures.put(borrowed.getId(), player2.getId());
            Forest ownLand = new Forest();
            harness.addToBattlefield(player1, ownLand);
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.playerHands.get(player2.getId())).contains(borrowedLand);
            assertThat(gd.playerHands.get(player1.getId())).contains(ownLand).doesNotContain(borrowedLand);
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).contains(multani);
        }

        @Test
        @DisplayName("Cannot pay the green mana requirement with colorless mana")
        void cannotActivateWithOnlyColorlessMana() {
            harness.setGraveyard(player1, List.of(new MultaniYavimayasAvatar()));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Earlier activation cannot return Multani after it leaves and reenters the graveyard")
        @CardUsed({MultaniYavimayasAvatar.class, Forest.class, MerfolkLooter.class})
        void earlierActivationLosesTrackOfDiscardedMultani() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setHand(player1, List.of());
            harness.setGraveyard(player1, List.of(multani));
            Permanent looter = addCreatureReady(player1, new MerfolkLooter());
            Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
            Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.activateGraveyardAbility(player1, 0);
            harness.handlePermanentChosen(player1, firstLand.getId());
            harness.handlePermanentChosen(player1, secondLand.getId());
            harness.activateGraveyardAbility(player1, 0);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).contains(multani);
            assertThat(gd.stack).hasSize(1);

            int looterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(looter);
            harness.activateAbility(player1, looterIndex, null, null);
            harness.passBothPriorities();
            int multaniIndex = gd.playerHands.get(player1.getId()).indexOf(multani);
            harness.handleCardChosen(player1, multaniIndex);
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(multani);

            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId())).contains(multani);
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(multani);
        }

        @Test
        @DisplayName("Can activate graveyard ability with enough mana and lands")
        void canActivateGraveyardAbility() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.GREEN, 2);

            int landsInHandBefore = (int) gd.playerHands.get(player1.getId()).stream()
                    .filter(c -> c.hasType(CardType.LAND)).count();

            harness.activateGraveyardAbility(player1, 0);

            // With exactly 2 lands, they are auto-returned to hand
            // Ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Multani, Yavimaya's Avatar");

            // Two lands should have been returned to hand
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
            int landsInHandAfter = (int) gd.playerHands.get(player1.getId()).stream()
                    .filter(c -> c.hasType(CardType.LAND)).count();
            assertThat(landsInHandAfter - landsInHandBefore).isEqualTo(2);
        }

        @Test
        @DisplayName("Resolving graveyard ability returns Multani to hand")
        void resolvingGraveyardAbilityReturnsToHand() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            // Multani should be in hand, not in graveyard
            harness.assertInHand(player1, "Multani, Yavimaya's Avatar");
            harness.assertNotInGraveyard(player1, "Multani, Yavimaya's Avatar");
        }

        @Test
        @DisplayName("Cannot activate without enough mana")
        void cannotActivateWithoutEnoughMana() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.GREEN, 1); // Not enough

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot activate without enough lands to return")
        void cannotActivateWithoutEnoughLands() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest()); // Only 1 land, need 2
            harness.addMana(player1, ManaColor.GREEN, 2);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough permanents");
        }

        @Test
        @DisplayName("Interactive choice when more than 2 lands available")
        void interactiveChoiceWithMoreThanTwoLands() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Plains());
            harness.addMana(player1, ManaColor.GREEN, 2);

            List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().hasType(CardType.LAND))
                    .toList();

            harness.activateGraveyardAbility(player1, 0);

            // Should be in interactive permanent choice mode (no stack entry yet)
            assertThat(gd.stack).isEmpty();

            // Choose two lands to return
            harness.handlePermanentChosen(player1, lands.get(0).getId());
            harness.handlePermanentChosen(player1, lands.get(1).getId());

            // Now ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Multani, Yavimaya's Avatar");

            // Two lands returned, one remains
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Mana cost is paid when activating")
        void manaCostIsPaid() {
            MultaniYavimayasAvatar multani = new MultaniYavimayasAvatar();
            harness.setGraveyard(player1, List.of(multani));
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        }
    }

}
