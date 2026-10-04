package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.k.KessigCagebreakers;
import com.github.laxika.magicalvibes.cards.m.MarkovPatrician;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.u.UnburialRites;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
class GarrukRelentlessTest extends BaseCardTest {

    // Front face — 0: Deal 3 damage to target creature; it deals power back

    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("Front face 0: fight ability")
    class FightAbility {

        @Test
        @DisplayName("Deals 3 damage to target creature and receives power damage back")
        void dealsDamageAndReceivesPowerBack() {
            Permanent garruk = addFrontFace(player1, 3);
            // Add a 2/2 creature
            Permanent target = addCreature(player2, new DarkthicketWolf());

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, target.getId(), null);
            harness.passBothPriorities();

            // Garruk dealt 3 damage to 2/2 creature — creature should die
            harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");
            // Creature had 2 power, so Garruk loses 2 loyalty: 3 - 2 = 1
            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        }

        @Test
        @DisplayName("Garruk transforms when loyalty drops to 2 or less after fight")
        void transformsAfterFightDroppingLoyaltyToTwo() {
            Permanent garruk = addFrontFace(player1, 3);
            // A creature with 2 power — Garruk goes to 3-2=1 loyalty, triggering transform
            Permanent target = addCreature(player2, new WalkingCorpse());

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, target.getId(), null);
            // First pass resolves the fight ability; state trigger pushes transform onto stack
            harness.passBothPriorities();
            // Second pass resolves the transform trigger
            harness.passBothPriorities();

            // Garruk should have transformed after state trigger resolved
            assertThat(garruk.isTransformed()).isTrue();
            assertThat(garruk.getCard().getName()).isEqualTo("Garruk, the Veil-Cursed");
        }

        @Test
        @DisplayName("Garruk dies if fight brings loyalty to 0")
        void garrukDiesIfFightBringsLoyaltyToZero() {
            Permanent garruk = addFrontFace(player1, 3);
            // A creature with 3 power — Garruk goes to 3-3=0
            Permanent target = addCreature(player2, new KessigCagebreakers());

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, target.getId(), null);
            harness.passBothPriorities();

            // Garruk should be dead (0 loyalty)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Garruk Relentless")
                            || p.getCard().getName().equals("Garruk, the Veil-Cursed"));
        }
    }

    // Front face — 0: Create a 2/2 green Wolf token

    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("Front face 0: create Wolf token")
    class CreateWolfToken {

        @Test
        @DisplayName("Creates a 2/2 green Wolf creature token")
        void createsWolfToken() {
            Permanent garruk = addFrontFace(player1, 3);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            // Loyalty stays at 3 (0-cost ability)
            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        }
    }

    // State trigger — transform when loyalty <= 2

    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("State trigger: transform at <= 2 loyalty")
    class StateTrigger {

        @Test
        @DisplayName("Garruk does not transform at 3 loyalty")
        void doesNotTransformAtThreeLoyalty() {
            Permanent garruk = addFrontFace(player1, 3);

            // Use the Wolf-creating ability (0-cost) — loyalty stays at 3
            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.isTransformed()).isFalse();
            assertThat(garruk.getCard().getName()).isEqualTo("Garruk Relentless");
        }
    }


    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("Back face +1: deathtouch Wolf token")
    class BackFacePlusOne {

        @Test
        @DisplayName("Creates a 1/1 black Wolf token with deathtouch")
        void createsBlackWolfWithDeathtouch() {
            Permanent garruk = addTransformedBackFace(player1, 3);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
            assertThat(token.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);
        }
    }


    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("Back face -1: sacrifice then search")
    class BackFaceMinusOne {

        @Test
        @DisplayName("With one creature, auto-sacrifices and searches library")
        void autoSacrificesOnlyCreatureAndSearches() {
            Permanent garruk = addTransformedBackFace(player1, 3);
            addCreature(player1, new DarkthicketWolf());

            // Put a creature card in the library for the search
            Card libraryCreature = new WalkingCorpse();
            harness.setLibrary(player1, List.of(libraryCreature));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

            // Creature was sacrificed
            harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");

            // Library search should be awaiting input
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null).isTrue();

            // Choose the creature from library
            harness.handleCardChosen(player1, 0);

            // The creature card should now be in hand
            harness.assertInHand(player1, "Walking Corpse");
        }

        @Test
        @DisplayName("With multiple creatures, prompts player to choose sacrifice target")
        void promptsForSacrificeWithMultipleCreatures() {
            Permanent garruk = addTransformedBackFace(player1, 3);
            Permanent creature1 = addCreature(player1, new DarkthicketWolf());
            addCreature(player1, new WalkingCorpse());

            Card libraryCreature = new WalkingCorpse();
            harness.setLibrary(player1, List.of(libraryCreature));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

            // Should be awaiting a permanent choice (sacrifice selection)
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();

            // Choose creature1 to sacrifice
            harness.handlePermanentChosen(player1, creature1.getId());

            // creature1 was sacrificed
            harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");

            // Library search should be awaiting input
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null).isTrue();

            // Choose the creature from library
            harness.handleCardChosen(player1, 0);

            harness.assertInHand(player1, "Walking Corpse");
        }

        @Test
        @DisplayName("With no creatures, nothing happens")
        void noCreaturesDoesNothing() {
            Permanent garruk = addTransformedBackFace(player1, 3);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            // No further input required
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }


    @CardUsed({GarrukRelentless.class, DarkthicketWolf.class, KessigCagebreakers.class, MarkovPatrician.class, WalkingCorpse.class, UnburialRites.class})
    @Nested
    @DisplayName("Back face -3: trample and graveyard-based boost")
    class BackFaceMinusThree {

        @Test
        @DisplayName("Grants trample and +X/+X where X = creature cards in graveyard")
        void grantsBoostBasedOnGraveyardCreatures() {
            Permanent garruk = addTransformedBackFace(player1, 5);
            Permanent creature = addCreature(player1, new DarkthicketWolf());

            // Put 3 creature cards in graveyard
            harness.setGraveyard(player1, List.of(
                    new WalkingCorpse(),
                    new WalkingCorpse(),
                    new WalkingCorpse()
            ));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 2, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

            // GrizzlyBears is 2/2 + 3/3 = 5/5
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
            assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("With no creature cards in graveyard, only grants trample (X=0)")
        void zeroCreaturesInGraveyardOnlyGrantsTrample() {
            Permanent garruk = addTransformedBackFace(player1, 5);
            Permanent creature = addCreature(player1, new DarkthicketWolf());

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 2, null, null);
            harness.passBothPriorities();

            // Still 2/2 (X=0)
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
            assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Does not boost opponent's creatures")
        void doesNotBoostOpponentCreatures() {
            Permanent garruk = addTransformedBackFace(player1, 5);
            Permanent oppCreature = addCreature(player2, new DarkthicketWolf());

            harness.setGraveyard(player1, List.of(
                    new WalkingCorpse()
            ));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 2, null, null);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, oppCreature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, oppCreature)).isEqualTo(2);
            assertThat(oppCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        }

        @Test
        @DisplayName("Cannot activate -3 with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent garruk = addTransformedBackFace(player1, 2);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            assertThatThrownBy(() -> harness.activateAbility(player1, garrukIdx, 2, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }


    private Permanent addFrontFace(Player player, int loyalty) {
        GarrukRelentless card = new GarrukRelentless();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTransformedBackFace(Player player, int loyalty) {
        GarrukRelentless card = new GarrukRelentless();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        // Simulate already transformed
        perm.setTransformed(true);
        perm.setCard(card.getBackFaceCard());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void returnDamageFromLifelinkCreatureGainsLife() {
        Permanent garruk = addFrontFace(player1, 5);
        Permanent target = addCreature(player2, new MarkovPatrician());
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.assertLife(player2, 23);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Markov Patrician");
    }

    @Test
    void transformationDoesNotAllowAnotherLoyaltyAbilityThisTurn() {
        Permanent garruk = addFrontFace(player1, 3);
        Permanent target = addCreature(player2, new WalkingCorpse());
        harness.activateAbility(player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(garruk.isTransformed()).isTrue();
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformTriggerStillResolvesIfLoyaltyIncreasesAboveTwo() {
        Permanent garruk = addFrontFace(player1, 2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        garruk.setCounterCount(CounterType.LOYALTY, 4);
        harness.passBothPriorities();
        assertThat(garruk.isTransformed()).isTrue();
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @CardUsed({SarkhanTheMasterless.class})
    void ultimateGrantsTrampleToGarrukWhenHeIsACreature() {
        Permanent garruk = addTransformedBackFace(player1, 5);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, garruk)).isTrue();
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, garruk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, garruk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, garruk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void ultimateLocksAmountAndAffectedCreaturesAtResolution() {
        addTransformedBackFace(player1, 5);
        Permanent creature = addCreature(player1, new DarkthicketWolf());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new UnburialRites()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        harness.setGraveyard(player1, List.of());
        Permanent lateCreature = addCreature(player1, new WalkingCorpse());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }
}
