package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDealDamageAndReceivePowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureSearchLibraryForCreatureToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarrukRelentlessTest extends BaseCardTest {

    // ==========================================================================
    // Card structure
    // ==========================================================================

    @Test
    @DisplayName("Front face has two 0-loyalty abilities and a state trigger")
    void frontFaceStructure() {
        GarrukRelentless card = new GarrukRelentless();
        assertThat(card.getActivatedAbilities()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getLoyaltyCost()).isEqualTo(0);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(PlaneswalkerDealDamageAndReceivePowerDamageEffect.class);
        assertThat(card.getActivatedAbilities().get(1).getLoyaltyCost()).isEqualTo(0);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("GarrukTheVeilCursed");
    }

    @Test
    @DisplayName("Back face has +1, -1, -3 loyalty abilities")
    void backFaceStructure() {
        GarrukTheVeilCursed backFace = new GarrukTheVeilCursed();
        assertThat(backFace.getActivatedAbilities()).hasSize(3);
        assertThat(backFace.getActivatedAbilities().get(0).getLoyaltyCost()).isEqualTo(1);
        assertThat(backFace.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        assertThat(backFace.getActivatedAbilities().get(1).getLoyaltyCost()).isEqualTo(-1);
        assertThat(backFace.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(SacrificeCreatureSearchLibraryForCreatureToHandEffect.class);
        assertThat(backFace.getActivatedAbilities().get(2).getLoyaltyCost()).isEqualTo(-3);
        assertThat(backFace.getActivatedAbilities().get(2).getEffects())
                .anyMatch(e -> e instanceof GrantKeywordEffect)
                .anyMatch(e -> e instanceof BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect);
    }

    // ==========================================================================
    // Front face — 0: Deal 3 damage to target creature; it deals power back
    // ==========================================================================

    @Nested
    @DisplayName("Front face 0: fight ability")
    class FightAbility {

        @Test
        @DisplayName("Deals 3 damage to target creature and receives power damage back")
        void dealsDamageAndReceivesPowerBack() {
            Permanent garruk = addFrontFace(player1, 3);
            // Add a 2/4 creature
            Permanent target = addCreature(player2, "GrizzlyBears", 2, 2);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, target.getId(), null);
            harness.passBothPriorities();

            // Garruk dealt 3 damage to 2/2 creature — creature should die
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            // Creature had 2 power, so Garruk loses 2 loyalty: 3 - 2 = 1
            assertThat(garruk.getLoyaltyCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Garruk transforms when loyalty drops to 2 or less after fight")
        void transformsAfterFightDroppingLoyaltyToTwo() {
            Permanent garruk = addFrontFace(player1, 3);
            // A creature with 2 power — Garruk goes to 3-2=1 loyalty, triggering transform
            Permanent target = addCreature(player2, "EliteVanguard", 2, 1);

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
            Permanent target = addCreature(player2, "BigCreature", 3, 4);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 0, target.getId(), null);
            harness.passBothPriorities();

            // Garruk should be dead (0 loyalty)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Garruk Relentless")
                            || p.getCard().getName().equals("Garruk, the Veil-Cursed"));
        }
    }

    // ==========================================================================
    // Front face — 0: Create a 2/2 green Wolf token
    // ==========================================================================

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
            assertThat(garruk.getLoyaltyCounters()).isEqualTo(3);

            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        }
    }

    // ==========================================================================
    // State trigger — transform when loyalty <= 2
    // ==========================================================================

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

    // ==========================================================================
    // Back face +1: Create a 1/1 black Wolf token with deathtouch
    // ==========================================================================

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

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);

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

    // ==========================================================================
    // Back face -1: Sacrifice a creature, then search library for creature to hand
    // ==========================================================================

    @Nested
    @DisplayName("Back face -1: sacrifice then search")
    class BackFaceMinusOne {

        @Test
        @DisplayName("With one creature, auto-sacrifices and searches library")
        void autoSacrificesOnlyCreatureAndSearches() {
            Permanent garruk = addTransformedBackFace(player1, 3);
            Permanent creature = addCreature(player1, "GrizzlyBears", 2, 2);

            // Put a creature card in the library for the search
            Card libraryCreature = createCreatureCard("Runeclaw Bear", 2, 2);
            gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(libraryCreature)));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(2);

            // Creature was sacrificed
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Library search should be awaiting input
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.LIBRARY_SEARCH)).isTrue();

            // Choose the creature from library
            gs.handleLibraryCardChosen(gd, player1, 0);

            // The creature card should now be in hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        }

        @Test
        @DisplayName("With multiple creatures, prompts player to choose sacrifice target")
        void promptsForSacrificeWithMultipleCreatures() {
            Permanent garruk = addTransformedBackFace(player1, 3);
            Permanent creature1 = addCreature(player1, "GrizzlyBears", 2, 2);
            Permanent creature2 = addCreature(player1, "EliteVanguard", 2, 1);

            Card libraryCreature = createCreatureCard("Runeclaw Bear", 2, 2);
            gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(libraryCreature)));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(2);

            // Should be awaiting a permanent choice (sacrifice selection)
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();

            // Choose creature1 to sacrifice
            harness.handlePermanentChosen(player1, creature1.getId());

            // creature1 was sacrificed
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Library search should be awaiting input
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.LIBRARY_SEARCH)).isTrue();

            // Choose the creature from library
            gs.handleLibraryCardChosen(gd, player1, 0);

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        }

        @Test
        @DisplayName("With no creatures, nothing happens")
        void noCreaturesDoesNothing() {
            Permanent garruk = addTransformedBackFace(player1, 3);

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(2);
            // No further input required
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    // ==========================================================================
    // Back face -3: Creatures get trample and +X/+X (X = creature cards in GY)
    // ==========================================================================

    @Nested
    @DisplayName("Back face -3: trample and graveyard-based boost")
    class BackFaceMinusThree {

        @Test
        @DisplayName("Grants trample and +X/+X where X = creature cards in graveyard")
        void grantsBoostBasedOnGraveyardCreatures() {
            Permanent garruk = addTransformedBackFace(player1, 5);
            Permanent creature = addCreature(player1, "GrizzlyBears", 2, 2);

            // Put 3 creature cards in graveyard
            harness.setGraveyard(player1, List.of(
                    createCreatureCard("Dead1", 1, 1),
                    createCreatureCard("Dead2", 1, 1),
                    createCreatureCard("Dead3", 1, 1)
            ));

            int garrukIdx = gd.playerBattlefields.get(player1.getId()).indexOf(garruk);
            harness.activateAbility(player1, garrukIdx, 2, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(2);

            // GrizzlyBears is 2/2 + 3/3 = 5/5
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
            assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("With no creature cards in graveyard, only grants trample (X=0)")
        void zeroCreaturesInGraveyardOnlyGrantsTrample() {
            Permanent garruk = addTransformedBackFace(player1, 5);
            Permanent creature = addCreature(player1, "GrizzlyBears", 2, 2);

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
            Permanent oppCreature = addCreature(player2, "GrizzlyBears", 2, 2);

            harness.setGraveyard(player1, List.of(
                    createCreatureCard("Dead1", 1, 1)
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

    // ==========================================================================
    // Helpers
    // ==========================================================================

    private Permanent addFrontFace(Player player, int loyalty) {
        GarrukRelentless card = new GarrukRelentless();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTransformedBackFace(Player player, int loyalty) {
        GarrukRelentless card = new GarrukRelentless();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(loyalty);
        perm.setSummoningSick(false);
        // Simulate already transformed
        perm.setTransformed(true);
        perm.setCard(card.getBackFaceCard());
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, String name, int power, int toughness) {
        Card card = createCreatureCard(name, power, toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCreatureCard(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
