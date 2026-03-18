package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidTargetServiceTest {

    @Mock private GameQueryService gameQueryService;

    @InjectMocks
    private ValidTargetService validTargetService;

    private GameData gameData;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        gameData = new GameData(UUID.randomUUID(), "test-game", player1Id, "Player1");
        gameData.playerIds.add(player1Id);
        gameData.playerIds.add(player2Id);
        gameData.orderedPlayerIds.add(player1Id);
        gameData.orderedPlayerIds.add(player2Id);
        gameData.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helpers =====

    private Card createCard() {
        Card card = new Card();
        card.setName("Test Card");
        card.setType(CardType.INSTANT);
        return card;
    }

    private Card createCreatureCard() {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent addPermanentToBattlefield(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        gameData.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    // =====================================================================
    // canPermanentBeTargetedBySpell
    // =====================================================================

    @Nested
    @DisplayName("canPermanentBeTargetedBySpell")
    class CanPermanentBeTargetedBySpell {

        @Test
        @DisplayName("returns true when no protection/hexproof/shroud applies")
        void returnsTrue_whenNothingBlocksTargeting() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when permanent has protection from spell color")
        void returnsFalse_whenProtectionFromColor() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasProtectionFrom(gameData, perm, CardColor.RED)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when permanent has protection from source card type")
        void returnsFalse_whenProtectionFromCardType() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasProtectionFromSourceCardTypes(perm, spell)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when permanent has protection from source subtypes")
        void returnsFalse_whenProtectionFromSubtypes() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasProtectionFromSourceSubtypes(perm, spell)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when permanent has shroud")
        void returnsFalse_whenShroud() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when opponent's permanent has hexproof")
        void returnsFalse_whenOpponentHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(player2Id);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when own permanent has hexproof")
        void returnsTrue_whenOwnHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(player1Id);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when opponent's permanent has CantBeTargetOfSpellsOrAbilitiesEffect")
        void returnsFalse_whenOpponentHasGrantedHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(player2Id);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when own permanent has CantBeTargetOfSpellsOrAbilitiesEffect")
        void returnsTrue_whenOwnGrantedHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(player1Id);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when permanent can't be targeted by spell color")
        void returnsFalse_whenCantBeTargetedBySpellColor() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.cantBeTargetedBySpellColor(gameData, perm, CardColor.RED)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when permanent can't be targeted by non-color sources")
        void returnsFalse_whenCantBeTargetedByNonColorSources() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, spell)).thenReturn(true);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when target filter rejects the permanent")
        void returnsFalse_whenTargetFilterRejects() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            PlayerPredicateTargetFilter filter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent");
            spell.setCastTimeTargetFilter(filter);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            doThrow(new IllegalStateException("invalid target"))
                    .when(gameQueryService).validateTargetFilter(eq(filter), eq(perm), any(FilterContext.class));

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when target filter accepts the permanent")
        void returnsTrue_whenTargetFilterAccepts() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            PlayerPredicateTargetFilter filter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent");
            spell.setCastTimeTargetFilter(filter);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            // validateTargetFilter does nothing (no throw) → accepted

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }
    }

    // =====================================================================
    // computeValidTargetsForSpell
    // =====================================================================

    @Nested
    @DisplayName("computeValidTargetsForSpell")
    class ComputeValidTargetsForSpell {

        @Test
        @DisplayName("returns creatures as valid permanent targets for 'any target' spell")
        void returnsCreatures_forAnyTargetSpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Card creatureCard = createCreatureCard();
            Permanent creature = addPermanentToBattlefield(player2Id, creatureCard);

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).contains(creature.getId());
            assertThat(response.validPlayerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
            assertThat(response.prompt()).isEqualTo("Select a target for Test Card");
        }

        @Test
        @DisplayName("excludes non-creature permanents from 'any target' spell targeting")
        void excludesNonCreatures_forAnyTargetSpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Card enchantmentCard = new Card();
            enchantmentCard.setName("Test Enchantment");
            enchantmentCard.setType(CardType.ENCHANTMENT);
            Permanent enchantment = addPermanentToBattlefield(player2Id, enchantmentCard);

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(false);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).doesNotContain(enchantment.getId());
        }

        @Test
        @DisplayName("returns only permanents for permanent-only targeting spell")
        void returnsOnlyPermanents_forPermanentOnlySpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Card creatureCard = createCreatureCard();
            Permanent creature = addPermanentToBattlefield(player2Id, creatureCard);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).contains(creature.getId());
            assertThat(response.validPlayerIds()).isEmpty();
        }

        @Test
        @DisplayName("returns only players for player-only targeting spell")
        void returnsOnlyPlayers_forPlayerOnlySpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).isEmpty();
            assertThat(response.validPlayerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
        }

        @Test
        @DisplayName("excludes already-selected targets")
        void excludesAlreadySelectedTargets() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Card creatureCard = createCreatureCard();
            Permanent creature = addPermanentToBattlefield(player2Id, creatureCard);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, List.of(creature.getId()));

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes already-selected player targets")
        void excludesAlreadySelectedPlayerTargets() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, List.of(player1Id));

            assertThat(response.validPlayerIds()).containsExactly(player2Id);
        }

        @Test
        @DisplayName("filters player with shroud")
        void filtersPlayer_withShroud() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            when(gameQueryService.playerHasShroud(gameData, player1Id)).thenReturn(false);
            when(gameQueryService.playerHasShroud(gameData, player2Id)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("filters opponent player with hexproof")
        void filtersOpponentPlayer_withHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            when(gameQueryService.playerHasHexproof(gameData, player2Id)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("allows targeting self even when opponent has hexproof — self hexproof check is skipped")
        void allowsSelfTarget_whenOpponentHasHexproof() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            when(gameQueryService.playerHasHexproof(gameData, player2Id)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            // Player1 (caster) can always target themselves; player2 blocked by hexproof
            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("restricts player targeting with OPPONENT relation filter")
        void restrictsToOpponent_withPlayerRelationFilter() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));
            spell.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent"));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPlayerIds()).containsExactly(player2Id);
        }

        @Test
        @DisplayName("restricts player targeting with SELF relation filter")
        void restrictsToSelf_withPlayerRelationFilter() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));
            spell.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.SELF), "target self"));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("sets prompt for multi-target spells")
        void setsMultiTargetPrompt() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            // Create a multi-target spell via the target builder API
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            Card creatureCard = createCreatureCard();
            addPermanentToBattlefield(player2Id, creatureCard);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.prompt()).isEqualTo("Select targets for Test Card");
        }

        @Test
        @DisplayName("excludes permanents with shroud from spell targeting")
        void excludesShroudPermanents() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Card creatureCard = createCreatureCard();
            Permanent creature = addPermanentToBattlefield(player2Id, creatureCard);

            when(gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("sets minTargets and maxTargets correctly")
        void setsMinMaxTargets() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.target(null, 1, 1).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.minTargets()).isEqualTo(1);
            assertThat(response.maxTargets()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty response when no valid targets exist and no allowed target types")
        void returnsEmpty_whenNoAllowedTargets() {
            Card spell = createCard();
            // No effects added → no allowed targets

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).isEmpty();
            assertThat(response.validPlayerIds()).isEmpty();
        }
    }

    // =====================================================================
    // computeValidTargetsForAbility
    // =====================================================================

    @Nested
    @DisplayName("computeValidTargetsForAbility")
    class ComputeValidTargetsForAbility {

        @Test
        @DisplayName("returns valid permanent targets for ability that targets permanents")
        void returnsPermanentTargets_forPermanentTargetingAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).contains(creature.getId());
            assertThat(response.validPlayerIds()).isEmpty();
            assertThat(response.minTargets()).isEqualTo(1);
            assertThat(response.maxTargets()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns valid player targets for ability that targets players")
        void returnsPlayerTargets_forPlayerTargetingAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetPlayerEffect(2)), "Deal 2 damage to target player");

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).isEmpty();
            assertThat(response.validPlayerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
        }

        @Test
        @DisplayName("returns both permanent and player targets for 'any target' ability")
        void returnsBothTargets_forAnyTargetAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)), "Deal 2 damage to any target");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).contains(creature.getId());
            assertThat(response.validPlayerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
        }

        @Test
        @DisplayName("excludes shroud permanents from ability targeting")
        void excludesShroudPermanents() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes opponent's hexproof permanent from ability targeting")
        void excludesOpponentHexproofPermanent() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.hasKeyword(gameData, creature, Keyword.HEXPROOF)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, creature.getId())).thenReturn(player2Id);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("allows targeting own hexproof permanent with ability")
        void allowsOwnHexproofPermanent() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent ownCreature = addPermanentToBattlefield(player1Id, createCreatureCard());

            when(gameQueryService.hasKeyword(gameData, ownCreature, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.hasKeyword(gameData, ownCreature, Keyword.HEXPROOF)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, ownCreature.getId())).thenReturn(player1Id);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).contains(ownCreature.getId());
        }

        @Test
        @DisplayName("excludes opponent's permanent with CantBeTargetOfSpellsOrAbilitiesEffect")
        void excludesGrantedHexproofPermanent() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasGrantedEffect(gameData, creature, CantBeTargetOfSpellsOrAbilitiesEffect.class)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, creature.getId())).thenReturn(player2Id);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes permanent with protection from source color for damage abilities")
        void excludesProtectionFromColor_forDamageAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasProtectionFrom(gameData, creature, CardColor.RED)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes permanent with protection from source card types for damage abilities")
        void excludesProtectionFromCardType_forDamageAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasProtectionFromSourceCardTypes(creature, sourceCard)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes permanent with protection from source subtypes for damage abilities")
        void excludesProtectionFromSubtypes_forDamageAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasProtectionFromSourceSubtypes(creature, sourceCard)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("excludes permanent that can't be targeted by non-color sources")
        void excludesCantBeTargetedByNonColor() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.cantBeTargetedByNonColorSources(gameData, creature, sourceCard)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("filters player targets with shroud for ability")
        void filtersPlayerWithShroud() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)), "Deal 2 damage to any target");

            when(gameQueryService.playerHasShroud(gameData, player1Id)).thenReturn(false);
            when(gameQueryService.playerHasShroud(gameData, player2Id)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("filters opponent player with hexproof for ability")
        void filtersOpponentPlayerWithHexproof() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)), "Deal 2 damage to any target");

            when(gameQueryService.playerHasHexproof(gameData, player2Id)).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("restricts ability player target with OPPONENT filter")
        void restrictsAbilityPlayerTarget_opponentFilter() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)), "Deal 2 damage to any target",
                    new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent"));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPlayerIds()).containsExactly(player2Id);
        }

        @Test
        @DisplayName("restricts ability player target with SELF filter")
        void restrictsAbilityPlayerTarget_selfFilter() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)), "Deal 2 damage to any target",
                    new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.SELF), "target self"));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPlayerIds()).containsExactly(player1Id);
        }

        @Test
        @DisplayName("sets correct prompt for single-target ability")
        void setsSingleTargetPrompt() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.prompt()).isEqualTo("Select a target for Test Creature ability");
        }

        @Test
        @DisplayName("overload without alreadySelectedIds delegates correctly")
        void overloadWithoutAlreadySelected() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage to target creature");

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).contains(creature.getId());
        }

        @Test
        @DisplayName("ability with target filter rejects non-matching permanents")
        void abilityTargetFilter_rejectsNonMatching() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            PlayerPredicateTargetFilter filter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent's creature");
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage", filter);

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            doThrow(new IllegalStateException("invalid"))
                    .when(gameQueryService).validateTargetFilter(eq(filter), eq(creature), any(FilterContext.class));

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(creature.getId());
        }
    }

    // =====================================================================
    // computeValidTargetsForAbility – blocking creature special case
    // =====================================================================

    @Nested
    @DisplayName("computeValidTargetsForAbility - DestroyCreatureBlockingThisEffect")
    class AbilityBlockingThis {

        @Test
        @DisplayName("only targets creatures blocking this permanent")
        void onlyTargetsBlockingCreatures() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DestroyCreatureBlockingThisEffect()), "Destroy target creature blocking this");

            Card blockerCard = createCreatureCard();
            Permanent blocker = addPermanentToBattlefield(player2Id, blockerCard);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0); // blocking at index 0

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).contains(blocker.getId());
        }

        @Test
        @DisplayName("excludes non-blocking creatures")
        void excludesNonBlockingCreatures() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DestroyCreatureBlockingThisEffect()), "Destroy target creature blocking this");

            Card nonBlockerCard = createCreatureCard();
            Permanent nonBlocker = addPermanentToBattlefield(player2Id, nonBlockerCard);
            nonBlocker.setBlocking(false);

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(nonBlocker.getId());
        }

        @Test
        @DisplayName("excludes creatures blocking a different permanent")
        void excludesBlockerOfDifferentPermanent() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DestroyCreatureBlockingThisEffect()), "Destroy target creature blocking this");

            Card blockerCard = createCreatureCard();
            Permanent blocker = addPermanentToBattlefield(player2Id, blockerCard);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(5); // blocking at index 5, not 0

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0); // source at index 0

            assertThat(response.validPermanentIds()).doesNotContain(blocker.getId());
        }

        @Test
        @DisplayName("excludes non-creature permanents even if blocking")
        void excludesNonCreatureBlockers() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DestroyCreatureBlockingThisEffect()), "Destroy target creature blocking this");

            Card enchantmentCard = new Card();
            enchantmentCard.setName("Test Enchantment");
            enchantmentCard.setType(CardType.ENCHANTMENT);
            Permanent enchantment = addPermanentToBattlefield(player2Id, enchantmentCard);
            enchantment.setBlocking(true);
            enchantment.addBlockingTarget(0);

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(false);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0);

            assertThat(response.validPermanentIds()).doesNotContain(enchantment.getId());
        }
    }

    // =====================================================================
    // computeValidTargetsForAbility – multi-target abilities
    // =====================================================================

    @Nested
    @DisplayName("computeValidTargetsForAbility - multi-target")
    class AbilityMultiTarget {

        @Test
        @DisplayName("multi-target ability with PlayerPredicateTargetFilter position targets players")
        void multiTarget_playerPosition() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            PlayerPredicateTargetFilter playerFilter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "target opponent");
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)),
                    "Deal 2 damage", List.of(playerFilter), 1, 2);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0, List.of());

            // Position filter determines this position targets players, but
            // isValidAbilityPlayerTarget uses ability.getTargetFilter() (null) — both players valid
            assertThat(response.validPlayerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
            assertThat(response.validPermanentIds()).isEmpty();
            assertThat(response.prompt()).isEqualTo("Select targets for Test Creature ability");
        }

        @Test
        @DisplayName("multi-target ability excludes already-selected targets")
        void multiTarget_excludesAlreadySelected() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            PlayerPredicateTargetFilter playerFilter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.ANY), "target player");
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)),
                    "Deal 2 damage", List.of(playerFilter, playerFilter), 2, 2);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0, List.of(player1Id));

            // Position 1 (index 1) is player filter, player1Id already selected
            assertThat(response.validPlayerIds()).containsExactly(player2Id);
        }

        @Test
        @DisplayName("multi-target ability with non-player position filter targets permanents")
        void multiTarget_permanentPosition() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            // Non-PlayerPredicateTargetFilter → targets permanents
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)),
                    "Deal 2 damage", List.of(), 1, 2);

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0, List.of());

            assertThat(response.validPermanentIds()).contains(creature.getId());
        }

        @Test
        @DisplayName("multi-target ability sets min and max targets from ability")
        void multiTarget_setsMinMaxFromAbility() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            PlayerPredicateTargetFilter playerFilter = new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.ANY), "target player");
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToAnyTargetEffect(2)),
                    "Deal 2 damage", List.of(playerFilter), 2, 3);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0, List.of());

            assertThat(response.minTargets()).isEqualTo(2);
            assertThat(response.maxTargets()).isEqualTo(3);
        }
    }

    // =====================================================================
    // hasValidTargetsForSpell
    // =====================================================================

    @Nested
    @DisplayName("hasValidTargetsForSpell")
    class HasValidTargetsForSpell {

        @Test
        @DisplayName("returns true when a valid permanent target exists")
        void returnsTrue_whenValidPermanentExists() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when no valid permanent targets and no player targeting")
        void returnsFalse_whenNoPermanentTargets() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            // No permanents on any battlefield

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when valid player targets exist")
        void returnsTrue_whenValidPlayerTargetExists() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when all permanents have shroud and no player targeting")
        void returnsFalse_whenAllShrouded() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());

            when(gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)).thenReturn(true);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true for spell that targets graveyard")
        void returnsTrue_forGraveyardTargetingSpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            CardEffect graveyardEffect = new CardEffect() {
                @Override
                public boolean canTargetGraveyard() { return true; }
            };
            spell.addEffect(EffectSlot.SPELL, graveyardEffect);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns true for spell that targets exile")
        void returnsTrue_forExileTargetingSpell() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            CardEffect exileEffect = new CardEffect() {
                @Override
                public boolean canTargetExile() { return true; }
            };
            spell.addEffect(EffectSlot.SPELL, exileEffect);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when player targets exist but all have shroud")
        void returnsFalse_whenAllPlayersShrouded() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));

            when(gameQueryService.playerHasShroud(gameData, player1Id)).thenReturn(true);
            when(gameQueryService.playerHasShroud(gameData, player2Id)).thenReturn(true);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true for 'any target' spell when permanents are shrouded but players are valid")
        void returnsTrue_anyTarget_whenPlayersValid() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Permanent creature = addPermanentToBattlefield(player2Id, createCreatureCard());
            when(gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)).thenReturn(true);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false for spell with no effects (no target types)")
        void returnsFalse_whenNoEffects() {
            Card spell = createCard();

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when 'any target' with valid creature on own battlefield")
        void returnsTrue_anyTarget_ownCreature() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            addPermanentToBattlefield(player1Id, createCreatureCard());

            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(true);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns true for multi-target spell that also allows player targets")
        void returnsTrue_multiTarget_withPlayerTargets() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false for multi-target with only player effects and all players shrouded")
        void returnsFalse_multiTarget_allPlayersShrouded() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
            spell.target(null, 1, 2).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));

            when(gameQueryService.playerHasShroud(gameData, player1Id)).thenReturn(true);
            when(gameQueryService.playerHasShroud(gameData, player2Id)).thenReturn(true);

            boolean result = validTargetService.hasValidTargetsForSpell(gameData, spell, player1Id);

            // Still might return true if there are valid permanent targets
            // With no permanents and all players shrouded, should be false
            assertThat(result).isFalse();
        }
    }

    // =====================================================================
    // Edge cases
    // =====================================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("spell with null alreadySelectedIds works correctly")
        void nullAlreadySelectedIds() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).hasSize(1);
        }

        @Test
        @DisplayName("ability with null alreadySelectedIds works correctly")
        void abilityNullAlreadySelectedIds() {
            Card sourceCard = createCreatureCard();
            sourceCard.setColor(CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}",
                    List.of(new DealDamageToTargetCreatureEffect(2)), "Deal 2 damage");

            addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForAbility(
                    gameData, sourceCard, ability, player1Id, 0, null);

            assertThat(response.validPermanentIds()).hasSize(1);
        }

        @Test
        @DisplayName("planeswalker is valid target for 'any target' spell")
        void planeswalkerIsValidAnyTarget() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Card planeswalkerCard = new Card();
            planeswalkerCard.setName("Test Planeswalker");
            planeswalkerCard.setType(CardType.PLANESWALKER);
            Permanent planeswalker = addPermanentToBattlefield(player2Id, planeswalkerCard);

            // isCreature returns false for planeswalker, but isPlaneswalker should allow it
            when(gameQueryService.isCreature(eq(gameData), any(Permanent.class))).thenReturn(false);

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).contains(planeswalker.getId());
        }

        @Test
        @DisplayName("hexproof with null controller from findPermanentController does not crash")
        void hexproof_nullControllerDoesNotCrash() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)).thenReturn(false);
            when(gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(null);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            // null controller → hexproof check skipped → true
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CantBeTargetOfSpellsOrAbilitiesEffect with null controller does not crash")
        void grantedHexproof_nullControllerDoesNotCrash() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            when(gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)).thenReturn(true);
            when(gameQueryService.findPermanentController(gameData, perm.getId())).thenReturn(null);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("spell with no color doesn't cause NPE in protection check")
        void spellWithNullColor() {
            Card spell = createCard();
            // color is null by default
            Card creatureCard = createCreatureCard();
            Permanent perm = new Permanent(creatureCard);

            boolean result = validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spell, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("multiple permanents on battlefield are all evaluated")
        void multiplePermanentsEvaluated() {
            Card spell = createCard();
            spell.setColor(CardColor.RED);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Permanent creature1 = addPermanentToBattlefield(player1Id, createCreatureCard());
            Permanent creature2 = addPermanentToBattlefield(player2Id, createCreatureCard());

            ValidTargetsResponse response = validTargetService.computeValidTargetsForSpell(
                    gameData, spell, player1Id, null);

            assertThat(response.validPermanentIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        }
    }
}
