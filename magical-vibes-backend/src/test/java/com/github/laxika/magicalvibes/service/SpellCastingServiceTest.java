package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpellCastingServiceTest extends BaseCardTest {

    private SpellCastingService svc() {
        return harness.getSpellCastingService();
    }

    private Card createCreature(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }

    private Card createInstant(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        return card;
    }

    private Card createSorcery(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost(manaCost);
        return card;
    }

    private Card createEnchantment(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost(manaCost);
        return card;
    }

    private Card createArtifact(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return card;
    }

    @Nested
    @DisplayName("playCard validation")
    class PlayCardValidation {

        @Test
        @DisplayName("Throws when game is not running")
        void throwsWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, null, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Game is not running");
        }

        @Test
        @DisplayName("Throws when card is not playable (insufficient mana)")
        void throwsWhenCardNotPlayable() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Expensive Creature", "{4}{G}{G}");
            harness.setHand(player1, List.of(creature));
            // No mana available — card should not be playable

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, null, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card is not playable");
        }

        @Test
        @DisplayName("Throws when graveyard card is not playable from graveyard")
        void throwsWhenGraveyardCardNotPlayable() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card land = createLand("Test Land");
            harness.setGraveyard(player1, List.of(land));

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, null, null, null, null, null, true, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card is not playable from graveyard");
        }
    }

    @Nested
    @DisplayName("Land casting")
    class LandCasting {

        @Test
        @DisplayName("Land bypasses the stack and enters battlefield directly")
        void landBypassesStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card land = createLand("Test Plains");
            harness.setHand(player1, List.of(land));

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Test Plains");
            harness.assertNotInHand(player1, "Test Plains");
        }

        @Test
        @DisplayName("Land increments landsPlayedThisTurn counter")
        void landIncrementsCounter() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card land = createLand("Test Plains");
            harness.setHand(player1, List.of(land));
            int beforeCount = gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(beforeCount + 1);
        }
    }

    @Nested
    @DisplayName("Creature casting")
    class CreatureCasting {

        @Test
        @DisplayName("Creature spell goes on the stack")
        void creatureGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Test Bear", "{1}{G}");
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.GREEN, 2);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Test Bear");
        }

        @Test
        @DisplayName("Creature casting removes card from hand")
        void creatureRemovedFromHand() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Test Bear", "{1}{G}");
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.GREEN, 2);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            harness.assertNotInHand(player1, "Test Bear");
        }

        @Test
        @DisplayName("Creature casting pays mana cost")
        void creaturePaysMana() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Test Bear", "{1}{G}");
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.GREEN, 2);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Creature casting increments spellsCastThisTurn")
        void creatureIncrementsSpellCount() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Test Bear", "{1}{G}");
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.GREEN, 2);
            int beforeCount = gd.spellsCastThisTurn.getOrDefault(player1.getId(), 0);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.spellsCastThisTurn.get(player1.getId())).isEqualTo(beforeCount + 1);
        }
    }

    @Nested
    @DisplayName("Instant casting")
    class InstantCasting {

        @Test
        @DisplayName("Instant goes on the stack as INSTANT_SPELL")
        void instantGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card instant = createInstant("Test Bolt", "{R}");
            instant.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
            harness.setHand(player1, List.of(instant));
            harness.addMana(player1, ManaColor.RED, 1);

            svc().playCard(gd, player1, 0, null, player2.getId(), null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        }

        @Test
        @DisplayName("Instant carries its spell effects on the stack entry")
        void instantCarriesEffects() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card instant = createInstant("Test Bolt", "{R}");
            instant.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
            harness.setHand(player1, List.of(instant));
            harness.addMana(player1, ManaColor.RED, 1);

            svc().playCard(gd, player1, 0, null, player2.getId(), null, null, null, false, null);

            assertThat(gd.stack.getLast().getEffectsToResolve()).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve().get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        }
    }

    @Nested
    @DisplayName("Sorcery casting")
    class SorceryCasting {

        @Test
        @DisplayName("Sorcery goes on the stack as SORCERY_SPELL")
        void sorceryGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card sorcery = createSorcery("Test Draw", "{U}");
            sorcery.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            harness.setHand(player1, List.of(sorcery));
            harness.addMana(player1, ManaColor.BLUE, 1);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        }
    }

    @Nested
    @DisplayName("Enchantment casting")
    class EnchantmentCasting {

        @Test
        @DisplayName("Enchantment goes on the stack as ENCHANTMENT_SPELL")
        void enchantmentGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card enchantment = createEnchantment("Test Enchantment", "{1}{W}");
            harness.setHand(player1, List.of(enchantment));
            harness.addMana(player1, ManaColor.WHITE, 2);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        }
    }

    @Nested
    @DisplayName("Artifact casting")
    class ArtifactCasting {

        @Test
        @DisplayName("Artifact goes on the stack as ARTIFACT_SPELL")
        void artifactGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card artifact = createArtifact("Test Artifact", "{2}");
            harness.setHand(player1, List.of(artifact));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            svc().playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        }
    }

    @Nested
    @DisplayName("X-cost spells")
    class XCostSpells {

        @Test
        @DisplayName("Throws when X value is negative")
        void throwsForNegativeX() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card xSpell = createInstant("Test X Spell", "{X}{R}");
            xSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(0));
            harness.setHand(player1, List.of(xSpell));
            harness.addMana(player1, ManaColor.RED, 5);

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, -1, player2.getId(), null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("X value cannot be negative");
        }

        @Test
        @DisplayName("Throws when not enough mana for X cost")
        void throwsWhenNotEnoughManaForX() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card xSpell = createInstant("Test X Spell", "{X}{R}");
            xSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(0));
            harness.setHand(player1, List.of(xSpell));
            harness.addMana(player1, ManaColor.RED, 3);

            // Have 3 red mana: 1R for base cost, leaves 2 for X. Asking for X=3 should fail
            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, 3, player2.getId(), null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }
    }

    @Nested
    @DisplayName("Modal spells (ChooseOneEffect)")
    class ModalSpells {

        @Test
        @DisplayName("Throws when mode index is out of range")
        void throwsForInvalidMode() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card modal = createSorcery("Test Modal", "{U}");
            modal.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Deal 2 damage", new DealDamageToAnyTargetEffect(2))
            )));
            harness.setHand(player1, List.of(modal));
            harness.addMana(player1, ManaColor.BLUE, 1);

            // Mode index 5 is out of bounds (0 and 1 are valid)
            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, 5, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid mode index: 5");
        }

        @Test
        @DisplayName("Unwraps chosen mode effect into stack entry")
        void unwrapsChosenMode() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card modal = createSorcery("Test Modal", "{U}");
            modal.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Draw two cards", new DrawCardEffect(2))
            )));
            harness.setHand(player1, List.of(modal));
            harness.addMana(player1, ManaColor.BLUE, 1);

            // Choose mode 1 (second option)
            svc().playCard(gd, player1, 0, 1, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve()).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
            DrawCardEffect effect = (DrawCardEffect) gd.stack.getLast().getEffectsToResolve().get(0);
            assertThat(effect.amount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Convoke")
    class Convoke {

        @Test
        @DisplayName("Throws when convoke creature not found on battlefield")
        void throwsWhenConvokeCreatureNotFound() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Convoke Creature", "{3}{G}");
            creature.setKeywords(EnumSet.of(Keyword.CONVOKE));
            harness.setHand(player1, List.of(creature));
            // Give enough mana so the card passes playability check (convoke validation is separate)
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID fakeCreatureId = UUID.randomUUID();

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, null, null, null, null, List.of(fakeCreatureId), false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Convoke creature not found on your battlefield");
        }

        @Test
        @DisplayName("Throws when convoke creature is already tapped")
        void throwsWhenConvokeCreatureTapped() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card convokeCard = createCreature("Convoke Beast", "{3}{G}");
            convokeCard.setKeywords(EnumSet.of(Keyword.CONVOKE));
            Card tapper = createCreature("Tapped Helper", "{G}");
            harness.addToBattlefield(player1, tapper);
            UUID tapperId = gd.playerBattlefields.get(player1.getId()).get(0).getId();
            gd.playerBattlefields.get(player1.getId()).get(0).tap();

            harness.setHand(player1, List.of(convokeCard));
            // Give enough mana so the card passes playability check (convoke validation is separate)
            harness.addMana(player1, ManaColor.GREEN, 4);

            assertThatThrownBy(() -> svc().playCard(gd, player1, 0, null, null, null, null, List.of(tapperId), false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("is already tapped");
        }

        @Test
        @DisplayName("Convoke taps creatures and reduces mana cost")
        void convokeTapsCreatures() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card convokeCard = createCreature("Convoke Beast", "{3}{G}");
            convokeCard.setKeywords(EnumSet.of(Keyword.CONVOKE));

            // Put a green creature on the battlefield to tap for convoke
            Card helper = new GrizzlyBears();
            harness.addToBattlefield(player1, helper);
            UUID helperId = gd.playerBattlefields.get(player1.getId()).get(0).getId();

            harness.setHand(player1, List.of(convokeCard));
            // {3}{G} cost, 1 creature convokes for G, so we need 3 mana total (instead of 4)
            harness.addMana(player1, ManaColor.GREEN, 3);

            svc().playCard(gd, player1, 0, null, null, null, null, List.of(helperId), false, null);

            // Verify creature was tapped
            Permanent helperPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(helperId))
                    .findFirst().orElse(null);
            assertThat(helperPerm).isNotNull();
            assertThat(helperPerm.isTapped()).isTrue();

            // Verify spell went on stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        }
    }

    @Nested
    @DisplayName("paySpellManaCost")
    class PaySpellManaCost {

        @Test
        @DisplayName("Does nothing for cards with no mana cost")
        void noOpForNullManaCost() {
            Card card = createLand("Test Land");

            ManaPool poolBefore = gd.playerManaPools.get(player1.getId());
            int totalBefore = poolBefore.getTotal();

            svc().paySpellManaCost(gd, player1.getId(), card, 0, List.of());

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(totalBefore);
        }

        @Test
        @DisplayName("Pays colored mana from pool")
        void paysColoredMana() {
            Card card = createCreature("Test Creature", "{1}{G}");
            harness.addMana(player1, ManaColor.GREEN, 2);

            svc().paySpellManaCost(gd, player1.getId(), card, 0, List.of());

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Pays X-cost from pool")
        void paysXCost() {
            Card card = createInstant("Test X Spell", "{X}{R}");
            harness.addMana(player1, ManaColor.RED, 4);

            svc().paySpellManaCost(gd, player1.getId(), card, 3, List.of());

            // Paid {R} (base) + 3 (X) = 4 total
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("finishSpellCast")
    class FinishSpellCast {

        @Test
        @DisplayName("Increments spellsCastThisTurn counter")
        void incrementsSpellCount() {
            int before = gd.spellsCastThisTurn.getOrDefault(player1.getId(), 0);

            svc().finishSpellCast(gd, player1.getId(), player1,
                    gd.playerHands.get(player1.getId()), createCreature("Dummy", "{G}"));

            assertThat(gd.spellsCastThisTurn.get(player1.getId())).isEqualTo(before + 1);
        }

        @Test
        @DisplayName("Clears priorityPassedBy set")
        void clearsPriorityPassed() {
            gd.priorityPassedBy.add(player1.getId());
            gd.priorityPassedBy.add(player2.getId());

            svc().finishSpellCast(gd, player1.getId(), player1,
                    gd.playerHands.get(player1.getId()), createCreature("Dummy", "{G}"));

            assertThat(gd.priorityPassedBy).isEmpty();
        }
    }

    @Nested
    @DisplayName("playCardFromExile")
    class PlayCardFromExile {

        @Test
        @DisplayName("Throws when game is not running")
        void throwsWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;

            assertThatThrownBy(() -> svc().playCardFromExile(gd, player1, UUID.randomUUID(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Game is not running");
        }

        @Test
        @DisplayName("Throws when no exiled cards exist")
        void throwsWhenNoExiledCards() {
            gd.playerExiledCards.remove(player1.getId());

            assertThatThrownBy(() -> svc().playCardFromExile(gd, player1, UUID.randomUUID(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No exiled cards");
        }

        @Test
        @DisplayName("Throws when player has no permission to play exiled card")
        void throwsWhenNoPermission() {
            Card exiledCard = createCreature("Exiled Creature", "{1}{G}");
            gd.playerExiledCards.put(player1.getId(), new java.util.ArrayList<>(List.of(exiledCard)));

            assertThatThrownBy(() -> svc().playCardFromExile(gd, player1, exiledCard.getId(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No permission to play this exiled card");
        }

        @Test
        @DisplayName("Throws when card not found in exile")
        void throwsWhenCardNotInExile() {
            UUID fakeId = UUID.randomUUID();
            gd.playerExiledCards.put(player1.getId(), new java.util.ArrayList<>());
            gd.exilePlayPermissions.put(fakeId, player1.getId());

            assertThatThrownBy(() -> svc().playCardFromExile(gd, player1, fakeId, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card not found in exile");
        }

        @Test
        @DisplayName("Land from exile goes directly onto battlefield")
        void landFromExileBypassesStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card land = createLand("Exiled Plains");
            gd.playerExiledCards.put(player1.getId(), new java.util.ArrayList<>(List.of(land)));
            gd.exilePlayPermissions.put(land.getId(), player1.getId());

            svc().playCardFromExile(gd, player1, land.getId(), 0, null);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Exiled Plains");
            assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        }

        @Test
        @DisplayName("Creature from exile goes on stack and pays mana")
        void creatureFromExileGoesOnStack() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Exiled Bear", "{1}{G}");
            gd.playerExiledCards.put(player1.getId(), new java.util.ArrayList<>(List.of(creature)));
            gd.exilePlayPermissions.put(creature.getId(), player1.getId());
            harness.addMana(player1, ManaColor.GREEN, 2);

            svc().playCardFromExile(gd, player1, creature.getId(), 0, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Exiled Bear");
            assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Playing from exile increments spellsCastThisTurn")
        void exilePlayIncrementsSpellCount() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            Card creature = createCreature("Exiled Bear", "{G}");
            gd.playerExiledCards.put(player1.getId(), new java.util.ArrayList<>(List.of(creature)));
            gd.exilePlayPermissions.put(creature.getId(), player1.getId());
            harness.addMana(player1, ManaColor.GREEN, 1);
            int before = gd.spellsCastThisTurn.getOrDefault(player1.getId(), 0);

            svc().playCardFromExile(gd, player1, creature.getId(), 0, null);

            assertThat(gd.spellsCastThisTurn.get(player1.getId())).isEqualTo(before + 1);
        }
    }
}
