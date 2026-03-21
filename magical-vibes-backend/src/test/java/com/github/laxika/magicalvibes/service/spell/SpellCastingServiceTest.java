package com.github.laxika.magicalvibes.service.spell;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpellCastingServiceTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private TurnProgressionService turnProgressionService;

    @Mock
    private TargetLegalityService targetLegalityService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @InjectMocks
    private SpellCastingService svc;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player1 = new Player(player1Id, "Player1");
        player2 = new Player(player2Id, "Player2");
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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

    private void setHand(UUID playerId, List<Card> cards) {
        List<Card> hand = gd.playerHands.get(playerId);
        hand.clear();
        hand.addAll(cards);
    }

    private void addMana(UUID playerId, ManaColor color, int amount) {
        gd.playerManaPools.get(playerId).add(color, amount);
    }

    // =========================================================================
    // playCard validation
    // =========================================================================

    @Nested
    @DisplayName("playCard validation")
    class PlayCardValidation {

        @Test
        @DisplayName("Throws when game is not running")
        void throwsWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, null, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Game is not running");

            verifyNoInteractions(battlefieldEntryService, turnProgressionService, triggerCollectionService);
        }

        @Test
        @DisplayName("Throws when card is not playable (insufficient mana)")
        void throwsWhenCardNotPlayable() {
            Card creature = createCreature("Expensive Creature", "{4}{G}{G}");
            setHand(player1Id, List.of(creature));
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, null, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card is not playable");

            verifyNoInteractions(battlefieldEntryService, turnProgressionService, triggerCollectionService);
        }

        @Test
        @DisplayName("Throws when graveyard card is not playable from graveyard")
        void throwsWhenGraveyardCardNotPlayable() {
            Card land = createLand("Test Land");
            gd.playerGraveyards.get(player1Id).add(land);
            when(gameBroadcastService.getPlayableGraveyardLandIndices(gd, player1Id)).thenReturn(List.of());

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, null, null, null, null, null, true, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card is not playable from graveyard");
        }
    }

    // =========================================================================
    // Land casting
    // =========================================================================

    @Nested
    @DisplayName("Land casting")
    class LandCasting {

        @Test
        @DisplayName("Land bypasses the stack and enters battlefield directly")
        void landBypassesStack() {
            Card land = createLand("Test Plains");
            setHand(player1Id, List.of(land));
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
            verify(battlefieldEntryService).processCreatureETBEffects(eq(gd), eq(player1Id), eq(land), any(), anyBoolean());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Land increments landsPlayedThisTurn counter")
        void landIncrementsCounter() {
            Card land = createLand("Test Plains");
            setHand(player1Id, List.of(land));
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));
            int beforeCount = gd.landsPlayedThisTurn.getOrDefault(player1Id, 0);

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.landsPlayedThisTurn.get(player1Id)).isEqualTo(beforeCount + 1);
            // Lands don't trigger spell cast triggers
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // Creature casting
    // =========================================================================

    @Nested
    @DisplayName("Creature casting")
    class CreatureCasting {

        @Test
        @DisplayName("Creature spell goes on the stack")
        void creatureGoesOnStack() {
            Card creature = createCreature("Test Bear", "{1}{G}");
            setHand(player1Id, List.of(creature));
            addMana(player1Id, ManaColor.GREEN, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Test Bear");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
            verify(gameBroadcastService).broadcastGameState(gd);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(creature), eq(player1Id), anyBoolean());
            verify(triggerCollectionService).checkBecomesTargetOfSpellTriggers(gd);
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Creature casting removes card from hand")
        void creatureRemovedFromHand() {
            Card creature = createCreature("Test Bear", "{1}{G}");
            setHand(player1Id, List.of(creature));
            addMana(player1Id, ManaColor.GREEN, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.playerHands.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Creature casting pays mana cost")
        void creaturePaysMana() {
            Card creature = createCreature("Test Bear", "{1}{G}");
            setHand(player1Id, List.of(creature));
            addMana(player1Id, ManaColor.GREEN, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            ManaPool pool = gd.playerManaPools.get(player1Id);
            assertThat(pool.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Creature casting increments spellsCastThisTurn")
        void creatureIncrementsSpellCount() {
            Card creature = createCreature("Test Bear", "{1}{G}");
            setHand(player1Id, List.of(creature));
            addMana(player1Id, ManaColor.GREEN, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));
            int beforeCount = gd.getSpellsCastThisTurnCount(player1Id);

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.getSpellsCastThisTurnCount(player1Id)).isEqualTo(beforeCount + 1);
        }
    }

    // =========================================================================
    // Instant casting
    // =========================================================================

    @Nested
    @DisplayName("Instant casting")
    class InstantCasting {

        @Test
        @DisplayName("Instant goes on the stack as INSTANT_SPELL")
        void instantGoesOnStack() {
            Card instant = createInstant("Test Bolt", "{R}");
            instant.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
            setHand(player1Id, List.of(instant));
            addMana(player1Id, ManaColor.RED, 1);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, player2Id, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
            verify(targetLegalityService).validateSpellTargeting(eq(gd), eq(instant), eq(player2Id), any(), eq(player1Id), anyBoolean());
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(instant), eq(player1Id), anyBoolean());
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Instant carries its spell effects on the stack entry")
        void instantCarriesEffects() {
            Card instant = createInstant("Test Bolt", "{R}");
            instant.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
            setHand(player1Id, List.of(instant));
            addMana(player1Id, ManaColor.RED, 1);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, player2Id, null, null, null, false, null);

            assertThat(gd.stack.getLast().getEffectsToResolve()).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve().get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        }
    }

    // =========================================================================
    // Sorcery casting
    // =========================================================================

    @Nested
    @DisplayName("Sorcery casting")
    class SorceryCasting {

        @Test
        @DisplayName("Sorcery goes on the stack as SORCERY_SPELL")
        void sorceryGoesOnStack() {
            Card sorcery = createSorcery("Test Draw", "{U}");
            sorcery.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            setHand(player1Id, List.of(sorcery));
            addMana(player1Id, ManaColor.BLUE, 1);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(sorcery), eq(player1Id), anyBoolean());
            verify(turnProgressionService).resolveAutoPass(gd);
        }
    }

    // =========================================================================
    // Enchantment casting
    // =========================================================================

    @Nested
    @DisplayName("Enchantment casting")
    class EnchantmentCasting {

        @Test
        @DisplayName("Enchantment goes on the stack as ENCHANTMENT_SPELL")
        void enchantmentGoesOnStack() {
            Card enchantment = createEnchantment("Test Enchantment", "{1}{W}");
            setHand(player1Id, List.of(enchantment));
            addMana(player1Id, ManaColor.WHITE, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(enchantment), eq(player1Id), anyBoolean());
            verify(turnProgressionService).resolveAutoPass(gd);
        }
    }

    // =========================================================================
    // Artifact casting
    // =========================================================================

    @Nested
    @DisplayName("Artifact casting")
    class ArtifactCasting {

        @Test
        @DisplayName("Artifact goes on the stack as ARTIFACT_SPELL")
        void artifactGoesOnStack() {
            Card artifact = createArtifact("Test Artifact", "{2}");
            setHand(player1Id, List.of(artifact));
            addMana(player1Id, ManaColor.COLORLESS, 2);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            svc.playCard(gd, player1, 0, null, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(artifact), eq(player1Id), anyBoolean());
            verify(turnProgressionService).resolveAutoPass(gd);
        }
    }

    // =========================================================================
    // X-cost spells
    // =========================================================================

    @Nested
    @DisplayName("X-cost spells")
    class XCostSpells {

        @Test
        @DisplayName("Throws when X value is negative")
        void throwsForNegativeX() {
            Card xSpell = createInstant("Test X Spell", "{X}{R}");
            xSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(0));
            setHand(player1Id, List.of(xSpell));
            addMana(player1Id, ManaColor.RED, 5);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, -1, player2Id, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("X value cannot be negative");
        }

        @Test
        @DisplayName("Throws when not enough mana for X cost")
        void throwsWhenNotEnoughManaForX() {
            Card xSpell = createInstant("Test X Spell", "{X}{R}");
            xSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(0));
            setHand(player1Id, List.of(xSpell));
            addMana(player1Id, ManaColor.RED, 3);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            // Have 3 red mana: 1R for base cost, leaves 2 for X. Asking for X=3 should fail
            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, 3, player2Id, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }
    }

    // =========================================================================
    // Modal spells (ChooseOneEffect)
    // =========================================================================

    @Nested
    @DisplayName("Modal spells (ChooseOneEffect)")
    class ModalSpells {

        @Test
        @DisplayName("Throws when mode index is out of range")
        void throwsForInvalidMode() {
            Card modal = createSorcery("Test Modal", "{U}");
            modal.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Deal 2 damage", new DealDamageToAnyTargetEffect(2))
            )));
            setHand(player1Id, List.of(modal));
            addMana(player1Id, ManaColor.BLUE, 1);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            // Mode index 5 is out of bounds (0 and 1 are valid)
            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, 5, null, null, null, null, false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid mode index: 5");
        }

        @Test
        @DisplayName("Unwraps chosen mode effect into stack entry")
        void unwrapsChosenMode() {
            Card modal = createSorcery("Test Modal", "{U}");
            modal.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Draw two cards", new DrawCardEffect(2))
            )));
            setHand(player1Id, List.of(modal));
            addMana(player1Id, ManaColor.BLUE, 1);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            // Choose mode 1 (second option)
            svc.playCard(gd, player1, 0, 1, null, null, null, null, false, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve()).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
            DrawCardEffect effect = (DrawCardEffect) gd.stack.getLast().getEffectsToResolve().get(0);
            assertThat(effect.amount()).isEqualTo(2);
        }
    }

    // =========================================================================
    // Convoke
    // =========================================================================

    @Nested
    @DisplayName("Convoke")
    class Convoke {

        @Test
        @DisplayName("Throws when convoke creature not found on battlefield")
        void throwsWhenConvokeCreatureNotFound() {
            Card creature = createCreature("Convoke Creature", "{3}{G}");
            creature.setKeywords(EnumSet.of(Keyword.CONVOKE));
            setHand(player1Id, List.of(creature));
            addMana(player1Id, ManaColor.GREEN, 4);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            UUID fakeCreatureId = UUID.randomUUID();

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, null, null, null, null, List.of(fakeCreatureId), false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Convoke creature not found on your battlefield");
        }

        @Test
        @DisplayName("Throws when convoke creature is already tapped")
        void throwsWhenConvokeCreatureTapped() {
            Card convokeCard = createCreature("Convoke Beast", "{3}{G}");
            convokeCard.setKeywords(EnumSet.of(Keyword.CONVOKE));
            Card tapper = createCreature("Tapped Helper", "{G}");
            Permanent tapperPerm = new Permanent(tapper);
            tapperPerm.tap();
            gd.playerBattlefields.get(player1Id).add(tapperPerm);

            setHand(player1Id, List.of(convokeCard));
            addMana(player1Id, ManaColor.GREEN, 4);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

            assertThatThrownBy(() -> svc.playCard(gd, player1, 0, null, null, null, null, List.of(tapperPerm.getId()), false, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("is already tapped");
        }

        @Test
        @DisplayName("Convoke taps creatures and reduces mana cost")
        void convokeTapsCreatures() {
            Card convokeCard = createCreature("Convoke Beast", "{3}{G}");
            convokeCard.setKeywords(EnumSet.of(Keyword.CONVOKE));

            // Put a green creature on the battlefield to tap for convoke
            Card helper = createCreature("Green Helper", "{1}{G}");
            helper.setColor(CardColor.GREEN);
            Permanent helperPerm = new Permanent(helper);
            gd.playerBattlefields.get(player1Id).add(helperPerm);
            UUID helperId = helperPerm.getId();

            setHand(player1Id, List.of(convokeCard));
            // {3}{G} cost, 1 creature convokes for G, so we need 3 mana total (instead of 4)
            addMana(player1Id, ManaColor.GREEN, 3);
            // Card not playable without convoke, but playable with 1 convoke creature
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id, 1)).thenReturn(List.of(0));
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

            svc.playCard(gd, player1, 0, null, null, null, null, List.of(helperId), false, null);

            // Verify creature was tapped
            Permanent helperResult = gd.playerBattlefields.get(player1Id).stream()
                    .filter(p -> p.getId().equals(helperId))
                    .findFirst().orElse(null);
            assertThat(helperResult).isNotNull();
            assertThat(helperResult.isTapped()).isTrue();

            // Verify spell went on stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            verify(triggerCollectionService).checkEnchantedPermanentTapTriggers(eq(gd), any(Permanent.class));
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(convokeCard), eq(player1Id), anyBoolean());
            verify(turnProgressionService).resolveAutoPass(gd);
        }
    }

    // =========================================================================
    // paySpellManaCost
    // =========================================================================

    @Nested
    @DisplayName("paySpellManaCost")
    class PaySpellManaCost {

        @Test
        @DisplayName("Does nothing for cards with no mana cost")
        void noOpForNullManaCost() {
            Card card = createLand("Test Land");

            ManaPool poolBefore = gd.playerManaPools.get(player1Id);
            int totalBefore = poolBefore.getTotal();

            svc.paySpellManaCost(gd, player1Id, card, 0, List.of());

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isEqualTo(totalBefore);
        }

        @Test
        @DisplayName("Pays colored mana from pool")
        void paysColoredMana() {
            Card card = createCreature("Test Creature", "{1}{G}");
            addMana(player1Id, ManaColor.GREEN, 2);

            svc.paySpellManaCost(gd, player1Id, card, 0, List.of());

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Pays X-cost from pool")
        void paysXCost() {
            Card card = createInstant("Test X Spell", "{X}{R}");
            addMana(player1Id, ManaColor.RED, 4);

            svc.paySpellManaCost(gd, player1Id, card, 3, List.of());

            // Paid {R} (base) + 3 (X) = 4 total
            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isEqualTo(0);
        }
    }

    // =========================================================================
    // finishSpellCast
    // =========================================================================

    @Nested
    @DisplayName("finishSpellCast")
    class FinishSpellCast {

        @Test
        @DisplayName("Increments spellsCastThisTurn counter")
        void incrementsSpellCount() {
            int before = gd.getSpellsCastThisTurnCount(player1Id);
            Card dummy = createCreature("Dummy", "{G}");

            svc.finishSpellCast(gd, player1Id, player1,
                    gd.playerHands.get(player1Id), dummy);

            assertThat(gd.getSpellsCastThisTurnCount(player1Id)).isEqualTo(before + 1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
            verify(gameBroadcastService).broadcastGameState(gd);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(dummy), eq(player1Id), anyBoolean());
            verify(triggerCollectionService).checkBecomesTargetOfSpellTriggers(gd);
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Clears priorityPassedBy set")
        void clearsPriorityPassed() {
            gd.priorityPassedBy.add(player1Id);
            gd.priorityPassedBy.add(player2Id);

            svc.finishSpellCast(gd, player1Id, player1,
                    gd.playerHands.get(player1Id), createCreature("Dummy", "{G}"));

            assertThat(gd.priorityPassedBy).isEmpty();
        }
    }

    // =========================================================================
    // playCardFromExile
    // =========================================================================

    @Nested
    @DisplayName("playCardFromExile")
    class PlayCardFromExile {

        @Test
        @DisplayName("Throws when game is not running")
        void throwsWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;

            assertThatThrownBy(() -> svc.playCardFromExile(gd, player1, UUID.randomUUID(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Game is not running");
        }

        @Test
        @DisplayName("Throws when no exiled cards exist")
        void throwsWhenNoExiledCards() {
            gd.exiledCards.clear();

            assertThatThrownBy(() -> svc.playCardFromExile(gd, player1, UUID.randomUUID(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No exiled cards");
        }

        @Test
        @DisplayName("Throws when player has no permission to play exiled card")
        void throwsWhenNoPermission() {
            Card exiledCard = createCreature("Exiled Creature", "{1}{G}");
            gd.addToExile(player1Id, exiledCard);

            assertThatThrownBy(() -> svc.playCardFromExile(gd, player1, exiledCard.getId(), 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No permission to play this exiled card");
        }

        @Test
        @DisplayName("Throws when card not found in exile")
        void throwsWhenCardNotInExile() {
            UUID fakeId = UUID.randomUUID();
            gd.exilePlayPermissions.put(fakeId, player1Id);
            // Add a dummy card to exile so the "no exiled cards" check passes
            gd.addToExile(player1Id, createCreature("Dummy Card", "{G}"));

            assertThatThrownBy(() -> svc.playCardFromExile(gd, player1, fakeId, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card not found in exile");
        }

        @Test
        @DisplayName("Land from exile goes directly onto battlefield")
        void landFromExileBypassesStack() {
            Card land = createLand("Exiled Plains");
            gd.addToExile(player1Id, land);
            gd.exilePlayPermissions.put(land.getId(), player1Id);

            svc.playCardFromExile(gd, player1, land.getId(), 0, null);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
            verify(battlefieldEntryService).processCreatureETBEffects(eq(gd), eq(player1Id), eq(land), any(), anyBoolean());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
            verify(turnProgressionService).resolveAutoPass(gd);
            // Lands from exile don't trigger spell cast triggers
            verifyNoInteractions(triggerCollectionService);
        }

        @Test
        @DisplayName("Creature from exile goes on stack and pays mana")
        void creatureFromExileGoesOnStack() {
            Card creature = createCreature("Exiled Bear", "{1}{G}");
            gd.addToExile(player1Id, creature);
            gd.exilePlayPermissions.put(creature.getId(), player1Id);
            addMana(player1Id, ManaColor.GREEN, 2);

            svc.playCardFromExile(gd, player1, creature.getId(), 0, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Exiled Bear");
            assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isEqualTo(0);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
            verify(gameBroadcastService).broadcastGameState(gd);
            verify(triggerCollectionService).checkSpellCastTriggers(eq(gd), eq(creature), eq(player1Id));
            verify(triggerCollectionService).checkBecomesTargetOfSpellTriggers(gd);
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Playing from exile increments spellsCastThisTurn")
        void exilePlayIncrementsSpellCount() {
            Card creature = createCreature("Exiled Bear", "{G}");
            gd.addToExile(player1Id, creature);
            gd.exilePlayPermissions.put(creature.getId(), player1Id);
            addMana(player1Id, ManaColor.GREEN, 1);
            int before = gd.getSpellsCastThisTurnCount(player1Id);

            svc.playCardFromExile(gd, player1, creature.getId(), 0, null);

            assertThat(gd.getSpellsCastThisTurnCount(player1Id)).isEqualTo(before + 1);
        }
    }
}
