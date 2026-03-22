package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpellCastTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @InjectMocks
    private SpellCastTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player2Id);

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private static Card createCard(String name, CardColor color) {
        Card card = createCard(name);
        card.setColor(color);
        return card;
    }

    private static Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — SpellCastTriggerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — SpellCastTriggerEffect")
    class AnyPlayerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Shrine of Burning Rage");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(null), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Shrine of Burning Rage");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Shrine of Burning Rage");
            CardPredicate filter = new CardPredicate() {};
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = new SpellCastTriggerEffect(filter, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("uses self-targeting stack entry when resolved effect is self-targeting")
        void selfTargetingStackEntry() {
            Permanent perm = createPermanent("Some Permanent");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(null), eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("adds to pending may abilities when wrapped in MayEffect")
        void addsToMayAbilitiesWhenMayEffect() {
            Permanent perm = createPermanent("Angel's Feather");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var spellCastTrigger = new SpellCastTriggerEffect(null, List.of(innerEffect));
            var mayEffect = new MayEffect(spellCastTrigger, "Gain 1 life?");
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(null), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, mayEffect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Angel's Feather");
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class AnyPlayerColorCounter {

        @Test
        @DisplayName("triggers when spell color matches and onlyOwnSpells is false")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes PutCountersOnSourceEffect with correct amount")
        void stackEntryIncludesCorrectEffect() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 2, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            var resolved = (PutCountersOnSourceEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.amount()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns false when spell color is null")
        void returnsFalseWhenColorNull() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Artifact Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("returns false when spell color does not match trigger colors")
        void returnsFalseWhenColorDoesNotMatch() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("returns false when onlyOwnSpells is true")
        void returnsFalseWhenOnlyOwnSpells() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, true);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds to pending may abilities when wrapped in MayEffect")
        void addsToMayAbilitiesWhenMayEffect() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var inner = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            var mayEffect = new MayEffect(inner, "Gain 1 life?");
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, mayEffect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().sourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — KnowledgePoolCastTriggerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — KnowledgePoolCastTriggerEffect")
    class AnyPlayerKnowledgePool {

        @Test
        @DisplayName("puts triggered ability on stack when cast from hand")
        void triggersWhenCastFromHand() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Knowledge Pool");
        }

        @Test
        @DisplayName("stack entry contains KnowledgePoolExileAndCastEffect with correct IDs")
        void stackEntryContainsCorrectEffect() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            var resolved = (KnowledgePoolExileAndCastEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.originalSpellCardId()).isEqualTo(spellCard.getId());
            assertThat(resolved.knowledgePoolPermanentId()).isEqualTo(perm.getId());
            assertThat(resolved.castingPlayerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns false when not cast from hand")
        void returnsFalseWhenNotCastFromHand() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherSubtypePermanentEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherSubtypePermanentEffect")
    class AnyPlayerCopySpellForSubtype {

        @Test
        @DisplayName("puts triggered ability on stack for instant targeting matching subtype permanent")
        void triggersForInstantTargetingSubtype() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Put the spell on the stack with a single target
            Permanent targetPerm = createPermanent("Goblin Guide");
            targetPerm.getCard().setSubtypes(List.of(CardSubtype.GOBLIN));
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(targetPerm.getId());
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, targetPerm.getId())).thenReturn(targetPerm);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            // Stack now has the original spell + the triggered ability
            assertThat(gd.stack).hasSize(2);
            var triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when trigger already has a spell snapshot")
        void returnsFalseWhenSnapshotNotNull() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var snapshot = new StackEntry(createCard("Dummy"), player1Id);
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(
                    CardSubtype.GOBLIN, snapshot, player1Id, UUID.randomUUID());
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell is not an instant or sorcery")
        void returnsFalseWhenNotInstantOrSorcery() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createCard("Grizzly Bears"); // creature
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell is not found on the stack")
        void returnsFalseWhenSpellNotOnStack() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Stack is empty — spell not found
            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when single target is a player ID")
        void returnsFalseWhenTargetIsPlayer() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(player2Id);
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when target permanent is not found on the battlefield")
        void returnsFalseWhenTargetPermanentNotFound() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            UUID missingPermanentId = UUID.randomUUID();
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(missingPermanentId);
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, missingPermanentId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when target permanent does not have matching subtype")
        void returnsFalseWhenSubtypeDoesNotMatch() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            Permanent targetPerm = createPermanent("Llanowar Elves");
            // No GOBLIN subtype
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(targetPerm.getId());
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, targetPerm.getId())).thenReturn(targetPerm);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherPlayerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherPlayerEffect")
    class AnyPlayerCopySpellForEachOtherPlayer {

        @Test
        @DisplayName("puts triggered ability on stack for instant on the stack")
        void triggersForInstantOnStack() {
            Permanent perm = createPermanent("Radiate");
            var effect = new CopySpellForEachOtherPlayerEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            var triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when spell is not found on the stack")
        void returnsFalseWhenSpellNotOnStack() {
            Permanent perm = createPermanent("Radiate");
            var effect = new CopySpellForEachOtherPlayerEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Stack is empty — spell not found
            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when trigger already has a spell snapshot")
        void returnsFalseWhenSnapshotNotNull() {
            Permanent perm = createPermanent("Radiate");
            var snapshot = new StackEntry(createCard("Dummy"), player1Id);
            var effect = new CopySpellForEachOtherPlayerEffect(snapshot, player1Id);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell is not an instant or sorcery")
        void returnsFalseWhenNotInstantOrSorcery() {
            Permanent perm = createPermanent("Radiate");
            var effect = new CopySpellForEachOtherPlayerEffect();
            Card spellCard = createCard("Grizzly Bears"); // creature
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class ControllerColorCounter {

        @Test
        @DisplayName("triggers when spell color matches trigger colors")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("returns false when spell color is null")
        void returnsFalseWhenColorNull() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Artifact Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell color is not in trigger colors (e.g. green for Quirion Dryad)")
        void returnsFalseWhenColorNotInTriggerColors() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Giant Growth", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — SpellCastTriggerEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — SpellCastTriggerEffect")
    class ControllerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter (non-targeting)")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Cabal Paladin");
            var innerEffect = new PutCountersOnSourceEffect(1, 1, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(null), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("puts targeting triggered ability into pendingSpellTargetTriggers")
        void putsTargetingTriggeredAbilityIntoPendingQueue() {
            Permanent perm = createPermanent("Guttersnipe");
            var innerEffect = new DealDamageToAnyTargetEffect(2);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(null), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingSpellTargetTriggers).hasSize(1);
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Guttersnipe");
            CardPredicate filter = new CardPredicate() {};
            var innerEffect = new DealDamageToAnyTargetEffect(2);
            var effect = new SpellCastTriggerEffect(filter, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — CastFromGraveyardTriggerEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — CastFromGraveyardTriggerEffect")
    class ControllerCastFromGraveyard {

        @Test
        @DisplayName("returns false when spell was cast from hand")
        void returnsFalseWhenCastFromHand() {
            Permanent perm = createPermanent("Snapcaster Mage");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("puts triggered ability on stack when cast from graveyard with non-targeting effects")
        void putsOnStackWhenNoTargetingNeeded() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(innerEffect);
        }

        @Test
        @DisplayName("adds to pending target triggers when effect needs targeting")
        void addsToPendingTargetTriggersWhenTargeting() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new DealDamageToAnyTargetEffect(3);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingSpellTargetTriggers).hasSize(1);
        }

        @Test
        @DisplayName("broadcasts log message when targeting is needed")
        void broadcastsLogWhenTargeting() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new DealDamageToAnyTargetEffect(3);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — DealDamageEqualToSpellManaValueToAnyTargetEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — DealDamageEqualToSpellManaValueToAnyTargetEffect")
    class ControllerManaValueDamage {

        @Test
        @DisplayName("adds to pending target triggers when spell matches filter")
        void addsToPendingTargetTriggers() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardPredicate() {};
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.pendingSpellTargetTriggers).hasSize(1);
        }

        @Test
        @DisplayName("resolved effect has damage equal to spell's mana value")
        void resolvedEffectHasCorrectDamage() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardPredicate() {};
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            var resolved = (DealDamageToAnyTargetEffect) gd.pendingSpellTargetTriggers.getFirst().effects().getFirst();
            assertThat(resolved.damage()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardPredicate() {};
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.pendingSpellTargetTriggers).isEmpty();
        }

        @Test
        @DisplayName("broadcasts log message with damage amount")
        void broadcastsLogMessage() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardPredicate() {};
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Kaervek the Merciless's triggered ability triggers — choose a target for 2 damage."));
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — GiveTargetPlayerPoisonCountersEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — GiveTargetPlayerPoisonCountersEffect")
    class ControllerPoisonOnSpellCast {

        @Test
        @DisplayName("adds to pending target triggers when spell matches filter")
        void addsToPendingTargetTriggers() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardPredicate() {};
            var effect = new GiveTargetPlayerPoisonCountersEffect(1, filter);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.pendingSpellTargetTriggers).hasSize(1);
            assertThat(gd.pendingSpellTargetTriggers.getFirst().playerTargetOnly()).isTrue();
        }

        @Test
        @DisplayName("returns false when spell filter is null")
        void returnsFalseWhenFilterNull() {
            Permanent perm = createPermanent("Hand of the Praetors");
            var effect = new GiveTargetPlayerPoisonCountersEffect(1);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardPredicate() {};
            var effect = new GiveTargetPlayerPoisonCountersEffect(1, filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("broadcasts log message")
        void broadcastsLogMessage() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardPredicate() {};
            var effect = new GiveTargetPlayerPoisonCountersEffect(1, filter);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessDiscardEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessDiscardEffect")
    class OpponentLoseLifeUnlessDiscard {

        @Test
        @DisplayName("puts triggered ability on stack with casting player as target")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Bloodchief Ascension");
            var effect = new LoseLifeUnlessDiscardEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Bloodchief Ascension");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("stack entry includes the LoseLifeUnlessDiscardEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Bloodchief Ascension");
            var effect = new LoseLifeUnlessDiscardEffect(3);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — CounterUnlessPaysEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — CounterUnlessPaysEffect")
    class OpponentCounterUnlessPays {

        @Test
        @DisplayName("puts triggered ability on stack targeting the spell on the stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Chalice of the Void");
            var effect = new CounterUnlessPaysEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Chalice of the Void");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(stackEntry.getTargetZone()).isEqualTo(Zone.STACK);
        }

        @Test
        @DisplayName("stack entry includes the CounterUnlessPaysEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Chalice of the Void");
            var effect = new CounterUnlessPaysEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — RevealTopCardCreatureToBattlefieldOrMayBottomEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — RevealTopCardCreatureToBattlefieldOrMayBottomEffect")
    class OpponentRevealTopCard {

        @Test
        @DisplayName("puts triggered ability on stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Lurking Predators");
            var effect = new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Lurking Predators");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("stack entry includes the effect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Lurking Predators");
            var effect = new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessPaysEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessPaysEffect")
    class OpponentLoseLifeUnlessPays {

        @Test
        @DisplayName("puts triggered ability on stack with casting player as target")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            var effect = new LoseLifeUnlessPaysEffect(2, 1);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Kambal, Consul of Allocation");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns false when spell does not match spell filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            CardPredicate filter = new CardPredicate() {};
            var effect = new LoseLifeUnlessPaysEffect(2, 1, filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(gameQueryService.matchesCardPredicate(eq(spellCard), eq(filter), eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("triggers when spell filter is null (no filter)")
        void triggersWhenNoFilter() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            var effect = new LoseLifeUnlessPaysEffect(2, 1);
            Card spellCard = createCard("Any Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class OpponentColorCounter {

        @Test
        @DisplayName("triggers when opponent's spell color matches")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Some Permanent");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.RED), 1, false);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("returns false when opponent's spell color does not match")
        void returnsFalseWhenColorDoesNotMatch() {
            Permanent perm = createPermanent("Some Permanent");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.RED), 1, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }
}
