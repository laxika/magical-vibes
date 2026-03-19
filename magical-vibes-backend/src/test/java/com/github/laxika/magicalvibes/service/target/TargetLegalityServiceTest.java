package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TargetLegalityServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private TargetValidationService targetValidationService;

    @InjectMocks
    private TargetLegalityService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
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
        gd.playerExiledCards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    // ===== Helpers =====

    private static Card createCreature(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card createCreatureWithKeyword(String name, CardColor color, Keyword keyword) {
        Card card = createCreature(name, color);
        card.setKeywords(EnumSet.of(keyword));
        return card;
    }

    private static Card createTargetingSpell(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(perm);
        lenient().when(gameQueryService.findPermanentById(gd, perm.getId())).thenReturn(perm);
        return perm;
    }

    // ===== validateSpellTargetOnStack =====

    @Nested
    @DisplayName("validateSpellTargetOnStack")
    class ValidateSpellTargetOnStack {

        @Test
        @DisplayName("throws when targetId is null")
        void throwsWhenTargetIdIsNull() {
            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, null, null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target a spell on the stack");
        }

        @Test
        @DisplayName("throws when spell is not on the stack")
        void throwsWhenSpellNotOnStack() {
            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, UUID.randomUUID(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts a spell on the stack")
        void acceptsSpellOnStack() {
            Card spell = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(spell, player2Id);
            gd.stack.add(entry);

            sut.validateSpellTargetOnStack(gd, spell.getId(), null, player1Id);
        }

        @Test
        @DisplayName("rejects an ability on the stack without HasTarget predicate")
        void rejectsAbilityWithoutHasTargetPredicate() {
            Card source = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player2Id, "test", List.of());
            gd.stack.add(entry);

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, source.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts an ability on the stack when filter contains HasTarget predicate")
        void acceptsAbilityWithHasTargetPredicate() {
            Card source = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player2Id, "test", List.of());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryHasTargetPredicate(), "error");

            sut.validateSpellTargetOnStack(gd, source.getId(), filter, player1Id);
        }

        @Test
        @DisplayName("throws when stack entry predicate does not match")
        void throwsWhenPredicateDoesNotMatch() {
            Card spell = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(spell, player2Id);
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                    "Must target an instant spell");

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, spell.getId(), filter, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an instant spell");
        }

        @Test
        @DisplayName("passes when stack entry predicate matches")
        void passesWhenPredicateMatches() {
            Card spell = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(spell, player2Id);
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                    "Must target a creature spell");

            sut.validateSpellTargetOnStack(gd, spell.getId(), filter, player1Id);
        }

        @Test
        @DisplayName("rejects triggered ability without HasTarget predicate")
        void rejectsTriggeredAbilityWithoutHasTargetPredicate() {
            Card source = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player2Id, "test", List.of());
            gd.stack.add(entry);

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, source.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts triggered ability with HasTarget predicate nested in AllOf")
        void acceptsAbilityWithNestedHasTargetPredicate() {
            Card source = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player2Id, "test", List.of());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryHasTargetPredicate(),
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.TRIGGERED_ABILITY))
                    )), "error");

            sut.validateSpellTargetOnStack(gd, source.getId(), filter, player1Id);
        }

        @Test
        @DisplayName("throws when HasTarget predicate present but target not found on stack")
        void throwsWhenHasTargetPredicateButTargetNotOnStack() {
            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryHasTargetPredicate(), "error");

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, UUID.randomUUID(), filter, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell or ability on the stack");
        }
    }

    // ===== validateSpellTargeting =====

    @Nested
    @DisplayName("validateSpellTargeting")
    class ValidateSpellTargeting {

        @Test
        @DisplayName("throws when target is not a permanent or player")
        void throwsWhenTargetIsInvalid() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, UUID.randomUUID(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("passes when targeting a valid permanent")
        void passesWhenTargetingValidPermanent() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id);
        }

        @Test
        @DisplayName("passes when targeting a player")
        void passesWhenTargetingPlayer() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, player2Id, null, player1Id);
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Shrouded Guy", CardColor.GREEN, Keyword.SHROUD));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasKeyword(gd, target, Keyword.HEXPROOF)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own target has hexproof")
        void passesWhenOwnTargetHasHexproof() {
            Permanent target = addPermanent(player1Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id);
        }

        @Test
        @DisplayName("throws when target has protection from spell color")
        void throwsWhenTargetHasProtectionFromSpellColor() {
            Permanent target = addPermanent(player2Id, createCreature("Crusader", CardColor.WHITE));
            Card spell = createTargetingSpell("Dark Bolt", CardColor.BLACK);
            when(gameQueryService.hasProtectionFrom(gd, target, CardColor.BLACK)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from black");
        }

        @Test
        @DisplayName("throws when target can't be targeted by spell color")
        void throwsWhenTargetCantBeTargetedBySpellColor() {
            Permanent target = addPermanent(player2Id, createCreature("Strider", CardColor.GREEN));
            Card spell = createTargetingSpell("Blue Bolt", CardColor.BLUE);
            when(gameQueryService.cantBeTargetedBySpellColor(gd, target, CardColor.BLUE)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be the target of blue spells");
        }

        @Test
        @DisplayName("throws when opponent's creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasGrantedEffect(eq(gd), eq(target), eq(CantBeTargetOfSpellsOrAbilitiesEffect.class))).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void passesWhenOwnTargetHasCantBeTargetEffect() {
            Permanent target = addPermanent(player1Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id);
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.playerHasShroud(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, player2Id, null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when player predicate filter rejects target (OPPONENT targeting self)")
        void throwsWhenPlayerPredicateRejectsTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            spell.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent"));

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, player1Id, null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an opponent");
        }

        @Test
        @DisplayName("passes when player predicate filter accepts target (OPPONENT targeting opponent)")
        void passesWhenPlayerPredicateAcceptsTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            spell.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent"));

            sut.validateSpellTargeting(gd, spell, player2Id, null, player1Id);
        }

        @Test
        @DisplayName("validates target filter on permanent target")
        void validatesTargetFilter() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Artifact Blast", CardColor.RED);
            spell.setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsArtifactPredicate(), "Target must be an artifact"));
            doThrow(new IllegalStateException("Target must be an artifact"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
        }

        @Test
        @DisplayName("throws when target has protection from source card type")
        void throwsWhenTargetHasProtectionFromSourceCardType() {
            Permanent target = addPermanent(player2Id, createCreature("Protected", CardColor.WHITE));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.hasProtectionFromSourceCardTypes(target, spell)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from");
        }

        @Test
        @DisplayName("throws when target has protection from source subtype")
        void throwsWhenTargetHasProtectionFromSourceSubtype() {
            Permanent target = addPermanent(player2Id, createCreature("Protected", CardColor.WHITE));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.hasProtectionFromSourceSubtypes(target, spell)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from source's subtype");
        }

        @Test
        @DisplayName("throws when target can't be targeted by non-color sources")
        void throwsWhenTargetCantBeTargetedByNonColorSources() {
            Permanent target = addPermanent(player2Id, createCreature("Gaea's Revenge", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.cantBeTargetedByNonColorSources(gd, target, spell)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be targeted by this source");
        }

        @Test
        @DisplayName("throws when opponent player target has hexproof")
        void throwsWhenOpponentPlayerTargetHasHexproof() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            when(gameQueryService.playerHasHexproof(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, player2Id, null, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }
    }

    // ===== validateActivatedAbilityTargeting =====

    @Nested
    @DisplayName("validateActivatedAbilityTargeting")
    class ValidateActivatedAbilityTargeting {

        @Test
        @DisplayName("passes with valid permanent target")
        void passesWithValidTarget() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            sut.validateActivatedAbilityTargeting(gd, player1Id, ability, List.of(),
                    target.getId(), null, sourceCard, 0);
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasKeyword(gd, target, Keyword.HEXPROOF)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own target has hexproof")
        void passesWhenOwnTargetHasHexproof() {
            Permanent target = addPermanent(player1Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            sut.validateActivatedAbilityTargeting(gd, player1Id, ability, List.of(),
                    target.getId(), null, sourceCard, 0);
        }

        @Test
        @DisplayName("throws when opponent's target has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasGrantedEffect(eq(gd), eq(target), eq(CantBeTargetOfSpellsOrAbilitiesEffect.class))).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("validates ability target filter on permanent")
        void validatesAbilityTargetFilter() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact"));
            doThrow(new IllegalStateException("Target must be an artifact"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
        }

        @Test
        @DisplayName("throws when OPPONENT predicate targets self")
        void throwsWhenOpponentPredicateTargetsSelf() {
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent"));

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), player1Id, null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an opponent");
        }

        @Test
        @DisplayName("throws when SELF predicate targets opponent")
        void throwsWhenSelfPredicateTargetsOpponent() {
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.SELF), "Must target yourself"));

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), player2Id, null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target yourself");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.playerHasShroud(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), player2Id, null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when target can't be targeted by non-color sources")
        void throwsWhenTargetCantBeTargetedByNonColorSources() {
            Permanent target = addPermanent(player2Id, createCreature("Gaea's Revenge", CardColor.GREEN));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.cantBeTargetedByNonColorSources(gd, target, sourceCard)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be targeted by this source");
        }

        @Test
        @DisplayName("passes when targetId is null")
        void passesWhenTargetIsNull() {
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            sut.validateActivatedAbilityTargeting(gd, player1Id, ability, List.of(),
                    null, null, sourceCard, 0);
        }

        @Test
        @DisplayName("throws when opponent player target has hexproof")
        void throwsWhenOpponentPlayerTargetHasHexproof() {
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");
            when(gameQueryService.playerHasHexproof(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1Id, ability,
                    List.of(), player2Id, null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }
    }

    // ===== validateMultiTargetAbility =====

    @Nested
    @DisplayName("validateMultiTargetAbility")
    class ValidateMultiTargetAbility {

        @Test
        @DisplayName("throws when too few targets")
        void throwsWhenTooFewTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 2, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 2 and 3 targets");
        }

        @Test
        @DisplayName("throws when too many targets")
        void throwsWhenTooManyTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear1", CardColor.GREEN));
            Permanent p2 = addPermanent(player2Id, createCreature("Bear2", CardColor.GREEN));
            Permanent p3 = addPermanent(player2Id, createCreature("Bear3", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId(), p2.getId(), p3.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 1 and 2 targets");
        }

        @Test
        @DisplayName("throws when targets are duplicated")
        void throwsWhenDuplicateTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId(), p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("All targets must be different");
        }

        @Test
        @DisplayName("throws when target list is null")
        void throwsWhenNullTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability, null, source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between");
        }

        @Test
        @DisplayName("passes with valid distinct targets")
        void passesWithValidTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear1", CardColor.GREEN));
            Permanent p2 = addPermanent(player2Id, createCreature("Bear2", CardColor.GREEN));

            sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId(), p2.getId()), source);
        }

        @Test
        @DisplayName("throws when permanent target has shroud")
        void throwsWhenPermanentTargetHasShroud() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2Id,
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            when(gameQueryService.hasKeyword(gd, p1, Keyword.SHROUD)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's permanent has hexproof")
        void throwsWhenOpponentPermanentHasHexproof() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            when(gameQueryService.hasKeyword(gd, p1, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.findPermanentController(gd, p1.getId())).thenReturn(player2Id);
            when(gameQueryService.hasKeyword(gd, p1, Keyword.HEXPROOF)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own permanent has hexproof")
        void passesWhenOwnPermanentHasHexproof() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player1Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            when(gameQueryService.findPermanentController(gd, p1.getId())).thenReturn(player1Id);

            sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(p1.getId()), source);
        }

        @Test
        @DisplayName("throws when permanent target is invalid")
        void throwsWhenInvalidPermanentTarget() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(UUID.randomUUID()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.ANY), "target player")),
                    1, 1);
            when(gameQueryService.playerHasShroud(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(player2Id), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when OPPONENT filter but targeting self")
        void throwsWhenOpponentFilterTargetsSelf() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent")),
                    1, 1);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(player1Id), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an opponent");
        }

        @Test
        @DisplayName("throws when SELF filter but targeting opponent")
        void throwsWhenSelfFilterTargetsOpponent() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.SELF), "Must target yourself")),
                    1, 1);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(player2Id), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target yourself");
        }

        @Test
        @DisplayName("throws when opponent's creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            Card source = createCreature("Source", CardColor.RED);
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasGrantedEffect(eq(gd), eq(target), eq(CantBeTargetOfSpellsOrAbilitiesEffect.class))).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(target.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("validates per-position target filter")
        void validatesPerPositionFilter() {
            Card source = createCreature("Source", CardColor.RED);
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PermanentPredicateTargetFilter(
                            new PermanentIsArtifactPredicate(), "Target must be an artifact")),
                    1, 1);
            doThrow(new IllegalStateException("Target must be an artifact"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(target.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
        }

        @Test
        @DisplayName("throws when target can't be targeted by non-color sources")
        void throwsWhenTargetCantBeTargetedByNonColorSources() {
            Card source = createCreature("Source", CardColor.RED);
            Permanent target = addPermanent(player2Id, createCreature("Gaea's Revenge", CardColor.GREEN));
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            when(gameQueryService.cantBeTargetedByNonColorSources(gd, target, source)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(target.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be targeted by this source");
        }

        @Test
        @DisplayName("throws when player position filter gets non-player target")
        void throwsWhenPlayerPositionFilterGetsNonPlayerTarget() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.ANY), "target player")),
                    1, 1);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1Id, ability,
                    List.of(UUID.randomUUID()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid player target");
        }
    }

    // ===== validateMultiSpellTargets =====

    @Nested
    @DisplayName("validateMultiSpellTargets")
    class ValidateMultiSpellTargets {

        private Card createMultiTargetSpell(int minTargets, int maxTargets) {
            Card card = new Card();
            card.setName("Multi Target Spell");
            card.setType(CardType.SORCERY);
            card.setManaCost("{R}");
            card.setColor(CardColor.RED);
            card.target(null, minTargets, maxTargets)
                    .addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
            return card;
        }

        @Test
        @DisplayName("throws when too few targets")
        void throwsWhenTooFewTargets() {
            Card spell = createMultiTargetSpell(2, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell, List.of(p1.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 2 and 3 targets");
        }

        @Test
        @DisplayName("throws when too many targets")
        void throwsWhenTooManyTargets() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear1", CardColor.GREEN));
            Permanent p2 = addPermanent(player2Id, createCreature("Bear2", CardColor.GREEN));
            Permanent p3 = addPermanent(player2Id, createCreature("Bear3", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(p1.getId(), p2.getId(), p3.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 1 and 2 targets");
        }

        @Test
        @DisplayName("throws when targets are duplicated")
        void throwsWhenDuplicateTargets() {
            Card spell = createMultiTargetSpell(1, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(p1.getId(), p1.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("All targets must be different");
        }

        @Test
        @DisplayName("throws when target is not on battlefield")
        void throwsWhenTargetNotOnBattlefield() {
            Card spell = createMultiTargetSpell(1, 2);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(UUID.randomUUID()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("throws when player target not allowed by spell effects")
        void throwsWhenPlayerTargetNotAllowed() {
            Card spell = new Card();
            spell.setName("Creature Only");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{R}");
            spell.setColor(CardColor.RED);
            spell.target(null, 1, 2);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(player2Id), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("This spell cannot target players");
        }

        @Test
        @DisplayName("passes with valid creature targets")
        void passesWithValidCreatureTargets() {
            Card spell = createMultiTargetSpell(1, 3);
            Permanent p1 = addPermanent(player2Id, createCreature("Bear1", CardColor.GREEN));
            Permanent p2 = addPermanent(player2Id, createCreature("Bear2", CardColor.GREEN));
            when(gameQueryService.isCreature(gd, p1)).thenReturn(true);
            when(gameQueryService.isCreature(gd, p2)).thenReturn(true);

            sut.validateMultiSpellTargets(gd, spell, List.of(p1.getId(), p2.getId()), player1Id);
        }

        @Test
        @DisplayName("passes when targeting player with spell that allows it")
        void passesWhenTargetingPlayerWithAllowedSpell() {
            Card spell = createMultiTargetSpell(1, 2);

            sut.validateMultiSpellTargets(gd, spell, List.of(player2Id), player1Id);
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasKeyword(gd, target, Keyword.HEXPROOF)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("throws when target has protection from spell color")
        void throwsWhenTargetHasProtectionFromColor() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setColor(CardColor.BLACK);
            Permanent target = addPermanent(player2Id, createCreature("Crusader", CardColor.WHITE));
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.hasProtectionFrom(gd, target, CardColor.BLACK)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from black");
        }

        @Test
        @DisplayName("throws when target can't be targeted by spell color")
        void throwsWhenTargetCantBeTargetedBySpellColor() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setColor(CardColor.BLUE);
            Permanent target = addPermanent(player2Id, createCreature("Strider", CardColor.GREEN));
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.cantBeTargetedBySpellColor(gd, target, CardColor.BLUE)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be the target of blue spells");
        }

        @Test
        @DisplayName("throws when non-creature target without target filter")
        void throwsWhenNonCreatureTargetWithoutFilter() {
            Card spell = createMultiTargetSpell(1, 2);
            Card artifact = new Card();
            artifact.setName("Test Artifact");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{1}");
            Permanent target = addPermanent(player2Id, artifact);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("is not a creature");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card spell = createMultiTargetSpell(1, 2);
            when(gameQueryService.playerHasShroud(gd, player2Id)).thenReturn(true);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(player2Id), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("validates target filter when present")
        void validatesTargetFilterWhenPresent() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"));
            Card artifact = new Card();
            artifact.setName("Test Artifact");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{1}");
            Permanent target = addPermanent(player2Id, artifact);
            doThrow(new IllegalStateException("Target must be a creature"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a creature");
        }

        @Test
        @DisplayName("validates per-position filter over card-level filter")
        void validatesPerPositionFilterOverCardFilter() {
            Card spell = new Card();
            spell.setName("Per-Position Spell");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{R}");
            spell.setColor(CardColor.RED);
            spell.setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"));
            spell.target(new PermanentPredicateTargetFilter(
                    new PermanentIsArtifactPredicate(), "Target must be an artifact"))
                    .addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            doThrow(new IllegalStateException("Target must be an artifact"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
        }
    }

    // ===== isTargetIllegalOnResolution =====

    @Nested
    @DisplayName("isTargetIllegalOnResolution")
    class IsTargetIllegalOnResolution {

        @Test
        @DisplayName("returns false for non-targeting entry")
        void returnsFalseForNonTargetingEntry() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id, "test", List.of());
            entry.setNonTargeting(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when target is still on battlefield")
        void returnsFalseWhenTargetStillOnBattlefield() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target left the battlefield")
        void returnsTrueWhenTargetLeftBattlefield() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, UUID.randomUUID(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when target is a player")
        void returnsFalseForPlayerTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, player2Id, Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target has shroud")
        void returnsTrueWhenTargetHasShroud() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns true when target has hexproof and is controlled by opponent")
        void returnsTrueWhenTargetHasHexproofAndControlledByOpponent() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.hasKeyword(gd, target, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasKeyword(gd, target, Keyword.HEXPROOF)).thenReturn(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when target has hexproof but is controlled by self")
        void returnsFalseWhenTargetHasHexproofButControlledBySelf() {
            Permanent target = addPermanent(player1Id,
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target has CantBeTargetOfSpellsOrAbilitiesEffect from opponent")
        void returnsTrueWhenTargetHasCantBeTargetEffect() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.hasGrantedEffect(eq(gd), eq(target), eq(CantBeTargetOfSpellsOrAbilitiesEffect.class))).thenReturn(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns true when target filter no longer matches")
        void returnsTrueWhenTargetFilterNoLongerMatches() {
            Card artifact = new Card();
            artifact.setName("Test Artifact");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{1}");
            Permanent target = addPermanent(player2Id, artifact);

            Card spell = createTargetingSpell("Destroy", CardColor.RED);
            spell.setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Destroy",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            doThrow(new IllegalStateException("Target must be a creature"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("uses entry target filter over card target filter")
        void usesEntryTargetFilterOverCardFilter() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Spell", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Spell",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            entry.setTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsArtifactPredicate(), "Target must be an artifact"));
            doThrow(new IllegalStateException("Target must be an artifact"))
                    .when(gameQueryService).validateTargetFilter(any(), eq(target), any());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Spell protection at resolution -----

        @Test
        @DisplayName("returns true when target gained spell-color protection after cast")
        void returnsTrueWhenTargetGainedSpellColorProtection() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.cantBeTargetedBySpellColor(gd, target, CardColor.RED)).thenReturn(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns true when target has non-color source restriction at resolution")
        void returnsTrueWhenTargetHasNonColorSourceRestriction() {
            Permanent target = addPermanent(player2Id, createCreature("Gaea's Revenge", CardColor.GREEN));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.cantBeTargetedByNonColorSources(gd, target, spell)).thenReturn(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when activated ability target has spell-color protection")
        void returnsFalseWhenAbilityTargetHasSpellColorProtection() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card source = createCreature("Source", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player1Id, "ability",
                    List.of(), 0, target.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        // ----- Graveyard targets -----

        @Test
        @DisplayName("returns false when graveyard target is present")
        void returnsFalseWhenGraveyardTargetPresent() {
            Card cardInGY = createCreature("Bear", CardColor.GREEN);

            Card spell = new Card();
            spell.setName("Raise Dead");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Raise Dead",
                    List.of(), cardInGY.getId(), Zone.GRAVEYARD);
            when(gameQueryService.findCardInGraveyardById(gd, cardInGY.getId())).thenReturn(cardInGY);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when graveyard target is gone")
        void returnsTrueWhenGraveyardTargetGone() {
            Card spell = new Card();
            spell.setName("Raise Dead");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Raise Dead",
                    List.of(), UUID.randomUUID(), Zone.GRAVEYARD);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Stack targets -----

        @Test
        @DisplayName("returns false when stack target is still on stack")
        void returnsFalseWhenStackTargetPresent() {
            Card targetSpell = createCreature("Bear", CardColor.GREEN);
            StackEntry targetEntry = new StackEntry(targetSpell, player2Id);
            gd.stack.add(targetEntry);

            Card counterSpell = new Card();
            counterSpell.setName("Counter");
            counterSpell.setType(CardType.INSTANT);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, counterSpell, player1Id, "Counter",
                    List.of(), targetSpell.getId(), Zone.STACK);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when stack target has resolved")
        void returnsTrueWhenStackTargetGone() {
            Card counterSpell = new Card();
            counterSpell.setName("Counter");
            counterSpell.setType(CardType.INSTANT);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, counterSpell, player1Id, "Counter",
                    List.of(), UUID.randomUUID(), Zone.STACK);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Multi-target permanents -----

        @Test
        @DisplayName("returns true when all multi-target permanents are gone")
        void returnsTrueWhenAllMultiTargetPermanentsGone() {
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(UUID.randomUUID(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when some multi-target permanents remain")
        void returnsFalseWhenSomeMultiTargetPermanentsRemain() {
            Permanent p1 = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(p1.getId(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when multi-target includes a player")
        void returnsFalseWhenMultiTargetIncludesPlayer() {
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(player2Id, UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        // ----- Multi-target card IDs (graveyard) -----

        @Test
        @DisplayName("returns true when all multi-target card IDs are gone from graveyard")
        void returnsTrueWhenAllTargetCardIdsGone() {
            Card spell = new Card();
            spell.setName("Exile Cards");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Exile",
                    List.of(), List.of(UUID.randomUUID(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when some multi-target card IDs remain in graveyard")
        void returnsFalseWhenSomeTargetCardIdsRemain() {
            Card cardInGY = createCreature("Bear", CardColor.GREEN);

            Card spell = new Card();
            spell.setName("Exile Cards");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1Id, "Exile",
                    List.of(), List.of(cardInGY.getId(), UUID.randomUUID()));
            when(gameQueryService.findCardInGraveyardById(gd, cardInGY.getId())).thenReturn(cardInGY);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when entry has no target at all")
        void returnsFalseWhenNoTargetAtAll() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }
    }

    // ===== matchesStackEntryPredicate =====

    @Nested
    @DisplayName("matchesStackEntryPredicate")
    class MatchesStackEntryPredicate {

        @Test
        @DisplayName("matches StackEntryTypeInPredicate")
        void matchesTypeInPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryTypeInPredicate when not matching")
        void rejectsTypeInPredicateWhenNotMatching() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryColorInPredicate")
        void matchesColorInPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryColorInPredicate(Set.of(CardColor.GREEN)), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryColorInPredicate when not matching")
        void rejectsColorInPredicateWhenNotMatching() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryColorInPredicate(Set.of(CardColor.RED)), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryIsSingleTargetPredicate with single target")
        void matchesSingleTargetPredicate() {
            Permanent target = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card card = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, player1Id, "Bolt",
                    card.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryIsSingleTargetPredicate(), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryIsSingleTargetPredicate when no target")
        void rejectsSingleTargetPredicateWhenNoTarget() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryIsSingleTargetPredicate(), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("StackEntryHasTargetPredicate always returns true")
        void hasTargetPredicateAlwaysReturnsTrue() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryHasTargetPredicate(), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("matches StackEntryManaValuePredicate")
        void matchesManaValuePredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            card.setManaCost("{1}{G}"); // mana value 2
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryManaValuePredicate(2), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryManaValuePredicate when not matching")
        void rejectsManaValuePredicateWhenNotMatching() {
            Card card = createCreature("Bear", CardColor.GREEN);
            card.setManaCost("{1}{G}"); // mana value 2
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryManaValuePredicate(3), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryTargetsYourPermanentPredicate when targeting your permanent")
        void matchesTargetsYourPermanentPredicate() {
            Permanent p1Creature = addPermanent(player1Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player2Id, "Bolt",
                    spell.getEffects(EffectSlot.SPELL), 0, p1Creature.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, p1Creature.getId())).thenReturn(player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryTargetsYourPermanentPredicate when not targeting your permanent")
        void rejectsTargetsYourPermanentPredicateWhenNotTargetingYours() {
            Permanent p2Creature = addPermanent(player2Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id, "Bolt",
                    spell.getEffects(EffectSlot.SPELL), 0, p2Creature.getId(), Map.of());
            when(gameQueryService.findPermanentController(gd, p2Creature.getId())).thenReturn(player2Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryTargetsYourPermanentPredicate via multi-target")
        void matchesTargetsYourPermanentPredicateViaMultiTarget() {
            Permanent p1Creature = addPermanent(player1Id, createCreature("Bear", CardColor.GREEN));
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player2Id, "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(p1Creature.getId()));
            when(gameQueryService.findPermanentController(gd, p1Creature.getId())).thenReturn(player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1Id))
                    .isTrue();
        }

        @Test
        @DisplayName("matches AllOfPredicate when all inner predicates match")
        void matchesAllOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.GREEN))
                    )), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects AllOfPredicate when one inner predicate does not match")
        void rejectsAllOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.RED))
                    )), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches AnyOfPredicate when one inner predicate matches")
        void matchesAnyOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAnyOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.GREEN))
                    )), player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects AnyOfPredicate when no inner predicate matches")
        void rejectsAnyOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAnyOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.RED))
                    )), player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("matches NotPredicate when inner does not match")
        void matchesNotPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(CardColor.RED))),
                    player2Id))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects NotPredicate when inner matches")
        void rejectsNotPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(CardColor.GREEN))),
                    player2Id))
                    .isFalse();
        }

        @Test
        @DisplayName("returns false for unknown predicate type")
        void returnsFalseForUnknownPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1Id);

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new UnknownPredicate(), player2Id))
                    .isFalse();
        }

        private record UnknownPredicate() implements StackEntryPredicate {}
    }

    // ===== validateEffectTargetInZone =====

    @Nested
    @DisplayName("validateEffectTargetInZone")
    class ValidateEffectTargetInZone {

        @Test
        @DisplayName("passes when card has no effects requiring validation")
        void passesWhenNoEffectsRequireValidation() {
            Card card = new Card();
            card.setName("Vanilla");
            card.setType(CardType.SORCERY);

            sut.validateEffectTargetInZone(gd, card, UUID.randomUUID(), Zone.BATTLEFIELD);
        }

        @Test
        @DisplayName("overload with xValue passes when card has no effects requiring validation")
        void passesWithXValueWhenNoEffects() {
            Card card = new Card();
            card.setName("Vanilla");
            card.setType(CardType.SORCERY);

            sut.validateEffectTargetInZone(gd, card, UUID.randomUUID(), Zone.GRAVEYARD, 3);
        }
    }

    // =========================================================================
    // validateGraveyardRetargetCandidate
    // =========================================================================

    @Nested
    @DisplayName("validateGraveyardRetargetCandidate")
    class ValidateGraveyardRetargetCandidate {

        @Test
        @DisplayName("throws when card is not in any graveyard")
        void throwsWhenCardNotInGraveyard() {
            Card spellCard = new Card();
            spellCard.setName("Raise Dead");
            spellCard.setType(CardType.SORCERY);

            UUID candidateId = UUID.randomUUID();
            when(gameQueryService.findCardInGraveyardById(gd, candidateId)).thenReturn(null);

            assertThatThrownBy(() -> sut.validateGraveyardRetargetCandidate(gd, spellCard, candidateId, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not in any graveyard");
        }

        @Test
        @DisplayName("passes when scope is ALL_GRAVEYARDS and card is in any graveyard")
        void passesWhenAllGraveyardsScope() {
            Card spellCard = new Card();
            spellCard.setName("Raise Dead");
            spellCard.setType(CardType.SORCERY);
            spellCard.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .targetGraveyard(true)
                    .build());

            Card graveyardCard = createCreature("Grizzly Bears", CardColor.GREEN);
            UUID candidateId = graveyardCard.getId();
            gd.playerGraveyards.get(player2Id).add(graveyardCard);

            when(gameQueryService.findCardInGraveyardById(gd, candidateId)).thenReturn(graveyardCard);

            sut.validateGraveyardRetargetCandidate(gd, spellCard, candidateId, player1Id);
        }

        @Test
        @DisplayName("throws when scope is CONTROLLERS_GRAVEYARD and card is in opponent's graveyard")
        void throwsWhenNotInControllersGraveyard() {
            Card spellCard = new Card();
            spellCard.setName("Disentomb");
            spellCard.setType(CardType.SORCERY);
            spellCard.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                    .targetGraveyard(true)
                    .build());

            Card opponentCard = createCreature("Serra Angel", CardColor.WHITE);
            UUID candidateId = opponentCard.getId();
            // Card is in opponent's graveyard (player2), but controller is player1
            gd.playerGraveyards.get(player2Id).add(opponentCard);

            when(gameQueryService.findCardInGraveyardById(gd, candidateId)).thenReturn(opponentCard);

            assertThatThrownBy(() -> sut.validateGraveyardRetargetCandidate(gd, spellCard, candidateId, player1Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not in controller's graveyard");
        }

        @Test
        @DisplayName("passes when scope is CONTROLLERS_GRAVEYARD and card is in controller's graveyard")
        void passesWhenInControllersGraveyard() {
            Card spellCard = new Card();
            spellCard.setName("Disentomb");
            spellCard.setType(CardType.SORCERY);
            spellCard.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                    .targetGraveyard(true)
                    .build());

            Card controllerCard = createCreature("Grizzly Bears", CardColor.GREEN);
            UUID candidateId = controllerCard.getId();
            // Card is in controller's graveyard (player1)
            gd.playerGraveyards.get(player1Id).add(controllerCard);

            when(gameQueryService.findCardInGraveyardById(gd, candidateId)).thenReturn(controllerCard);

            sut.validateGraveyardRetargetCandidate(gd, spellCard, candidateId, player1Id);
        }
    }
}
