package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.Asceticism;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarplusanStrider;
import com.github.laxika.magicalvibes.cards.m.MirranCrusader;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
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
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TargetLegalityServiceTest extends BaseCardTest {

    private TargetLegalityService sut;

    @BeforeEach
    void setUpService() {
        TargetValidatorRegistry registry = new TargetValidatorRegistry();
        TargetValidationService targetValidationService = new TargetValidationService(gqs, registry);
        sut = new TargetLegalityService(gqs, targetValidationService);
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
        return perm;
    }

    // ===== validateSpellTargetOnStack =====

    @Nested
    @DisplayName("validateSpellTargetOnStack")
    class ValidateSpellTargetOnStack {

        @Test
        @DisplayName("throws when targetId is null")
        void throwsWhenTargetIdIsNull() {
            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, null, null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target a spell on the stack");
        }

        @Test
        @DisplayName("throws when spell is not on the stack")
        void throwsWhenSpellNotOnStack() {
            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, UUID.randomUUID(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts a spell on the stack")
        void acceptsSpellOnStack() {
            Card spell = new GrizzlyBears();
            StackEntry entry = new StackEntry(spell, player2.getId());
            gd.stack.add(entry);

            sut.validateSpellTargetOnStack(gd, spell.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("rejects an ability on the stack without HasTarget predicate")
        void rejectsAbilityWithoutHasTargetPredicate() {
            Card source = new GrizzlyBears();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player2.getId(), "test", List.of());
            gd.stack.add(entry);

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, source.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts an ability on the stack when filter contains HasTarget predicate")
        void acceptsAbilityWithHasTargetPredicate() {
            Card source = new GrizzlyBears();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player2.getId(), "test", List.of());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryHasTargetPredicate(), "error");

            sut.validateSpellTargetOnStack(gd, source.getId(), filter, player1.getId());
        }

        @Test
        @DisplayName("throws when stack entry predicate does not match")
        void throwsWhenPredicateDoesNotMatch() {
            Card spell = new GrizzlyBears();
            StackEntry entry = new StackEntry(spell, player2.getId());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                    "Must target an instant spell");

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, spell.getId(), filter, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an instant spell");
        }

        @Test
        @DisplayName("passes when stack entry predicate matches")
        void passesWhenPredicateMatches() {
            Card spell = new GrizzlyBears();
            StackEntry entry = new StackEntry(spell, player2.getId());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                    "Must target a creature spell");

            sut.validateSpellTargetOnStack(gd, spell.getId(), filter, player1.getId());
        }

        @Test
        @DisplayName("rejects triggered ability without HasTarget predicate")
        void rejectsTriggeredAbilityWithoutHasTargetPredicate() {
            Card source = new GrizzlyBears();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player2.getId(), "test", List.of());
            gd.stack.add(entry);

            assertThatThrownBy(() -> sut.validateSpellTargetOnStack(gd, source.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a spell on the stack");
        }

        @Test
        @DisplayName("accepts triggered ability with HasTarget predicate nested in AllOf")
        void acceptsAbilityWithNestedHasTargetPredicate() {
            Card source = new GrizzlyBears();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player2.getId(), "test", List.of());
            gd.stack.add(entry);

            StackEntryPredicateTargetFilter filter = new StackEntryPredicateTargetFilter(
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryHasTargetPredicate(),
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.TRIGGERED_ABILITY))
                    )), "error");

            sut.validateSpellTargetOnStack(gd, source.getId(), filter, player1.getId());
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

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, UUID.randomUUID(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("passes when targeting a valid permanent")
        void passesWhenTargetingValidPermanent() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("passes when targeting a player")
        void passesWhenTargetingPlayer() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, player2.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Shrouded Guy", CardColor.GREEN, Keyword.SHROUD));
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own target has hexproof")
        void passesWhenOwnTargetHasHexproof() {
            Permanent target = addPermanent(player1.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("throws when target has protection from spell color")
        void throwsWhenTargetHasProtectionFromSpellColor() {
            // MirranCrusader has protection from BLACK and GREEN
            Permanent target = addPermanent(player2.getId(), new MirranCrusader());
            Card spell = createTargetingSpell("Dark Bolt", CardColor.BLACK);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from black");
        }

        @Test
        @DisplayName("throws when target can't be targeted by spell color")
        void throwsWhenTargetCantBeTargetedBySpellColor() {
            // KarplusanStrider: CantBeTargetedBySpellColorsEffect for BLUE and BLACK
            Permanent target = addPermanent(player2.getId(), new KarplusanStrider());
            Card spell = createTargetingSpell("Blue Bolt", CardColor.BLUE);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be the target of blue spells");
        }

        @Test
        @DisplayName("throws when opponent's creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            // Asceticism grants CantBeTargetOfSpellsOrAbilitiesEffect to own creatures
            addPermanent(player2.getId(), new Asceticism());
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void passesWhenOwnTargetHasCantBeTargetEffect() {
            // Asceticism grants CantBeTargetOfSpellsOrAbilitiesEffect to own creatures
            addPermanent(player1.getId(), new Asceticism());
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            // TrueBeliever grants controller shroud
            addPermanent(player2.getId(), new TrueBeliever());
            Card spell = createTargetingSpell("Burn", CardColor.RED);

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, player2.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when player predicate filter rejects target (OPPONENT targeting self)")
        void throwsWhenPlayerPredicateRejectsTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            spell.setTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent"));

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, player1.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target an opponent");
        }

        @Test
        @DisplayName("passes when player predicate filter accepts target (OPPONENT targeting opponent)")
        void passesWhenPlayerPredicateAcceptsTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            spell.setTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must target an opponent"));

            sut.validateSpellTargeting(gd, spell, player2.getId(), null, player1.getId());
        }

        @Test
        @DisplayName("validates target filter on permanent target")
        void validatesTargetFilter() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Artifact Blast", CardColor.RED);
            spell.setTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsArtifactPredicate(), "Target must be an artifact"));

            assertThatThrownBy(() -> sut.validateSpellTargeting(gd, spell, target.getId(), null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
        }
    }

    // ===== validateActivatedAbilityTargeting =====

    @Nested
    @DisplayName("validateActivatedAbilityTargeting")
    class ValidateActivatedAbilityTargeting {

        @Test
        @DisplayName("passes with valid permanent target")
        void passesWithValidTarget() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability, List.of(),
                    target.getId(), null, sourceCard, 0);
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own target has hexproof")
        void passesWhenOwnTargetHasHexproof() {
            Permanent target = addPermanent(player1.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability, List.of(),
                    target.getId(), null, sourceCard, 0);
        }

        @Test
        @DisplayName("throws when opponent's target has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            addPermanent(player2.getId(), new Asceticism());
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), target.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("validates ability target filter on permanent")
        void validatesAbilityTargetFilter() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact"));

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
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

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), player1.getId(), null, sourceCard, 0))
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

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), player2.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target yourself");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            addPermanent(player2.getId(), new TrueBeliever());
            Card sourceCard = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test");

            assertThatThrownBy(() -> sut.validateActivatedAbilityTargeting(gd, player1.getId(), ability,
                    List.of(), player2.getId(), null, sourceCard, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
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
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 2 and 3 targets");
        }

        @Test
        @DisplayName("throws when too many targets")
        void throwsWhenTooManyTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p2 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p3 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId(), p2.getId(), p3.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 1 and 2 targets");
        }

        @Test
        @DisplayName("throws when targets are duplicated")
        void throwsWhenDuplicateTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId(), p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("All targets must be different");
        }

        @Test
        @DisplayName("throws when target list is null")
        void throwsWhenNullTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability, null, source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between");
        }

        @Test
        @DisplayName("passes with valid distinct targets")
        void passesWithValidTargets() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 3);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p2 = addPermanent(player2.getId(), new GrizzlyBears());

            sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId(), p2.getId()), source);
        }

        @Test
        @DisplayName("throws when permanent target has shroud")
        void throwsWhenPermanentTargetHasShroud() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's permanent has hexproof")
        void throwsWhenOpponentPermanentHasHexproof() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("passes when own permanent has hexproof")
        void passesWhenOwnPermanentHasHexproof() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);
            Permanent p1 = addPermanent(player1.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));

            sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(p1.getId()), source);
        }

        @Test
        @DisplayName("throws when permanent target is invalid")
        void throwsWhenInvalidPermanentTarget() {
            Card source = createCreature("Source", CardColor.RED);
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(UUID.randomUUID()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card source = createCreature("Source", CardColor.RED);
            addPermanent(player2.getId(), new TrueBeliever());
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PlayerPredicateTargetFilter(
                            new PlayerRelationPredicate(PlayerRelation.ANY), "target player")),
                    1, 1);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(player2.getId()), source))
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

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(player1.getId()), source))
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

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(player2.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Must target yourself");
        }

        @Test
        @DisplayName("throws when opponent's creature has CantBeTargetOfSpellsOrAbilitiesEffect")
        void throwsWhenTargetHasCantBeTargetEffect() {
            Card source = createCreature("Source", CardColor.RED);
            addPermanent(player2.getId(), new Asceticism());
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test", List.of(), 1, 2);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(target.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("validates per-position target filter")
        void validatesPerPositionFilter() {
            Card source = createCreature("Source", CardColor.RED);
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            ActivatedAbility ability = new ActivatedAbility(true, "{R}", List.of(), "test",
                    List.of(new PermanentPredicateTargetFilter(
                            new PermanentIsArtifactPredicate(), "Target must be an artifact")),
                    1, 1);

            assertThatThrownBy(() -> sut.validateMultiTargetAbility(gd, player1.getId(), ability,
                    List.of(target.getId()), source))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be an artifact");
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
            card.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
            card.setMinTargets(minTargets);
            card.setMaxTargets(maxTargets);
            return card;
        }

        @Test
        @DisplayName("throws when too few targets")
        void throwsWhenTooFewTargets() {
            Card spell = createMultiTargetSpell(2, 3);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell, List.of(p1.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 2 and 3 targets");
        }

        @Test
        @DisplayName("throws when too many targets")
        void throwsWhenTooManyTargets() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p2 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p3 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(p1.getId(), p2.getId(), p3.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must target between 1 and 2 targets");
        }

        @Test
        @DisplayName("throws when targets are duplicated")
        void throwsWhenDuplicateTargets() {
            Card spell = createMultiTargetSpell(1, 3);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(p1.getId(), p1.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("All targets must be different");
        }

        @Test
        @DisplayName("throws when target is not on battlefield")
        void throwsWhenTargetNotOnBattlefield() {
            Card spell = createMultiTargetSpell(1, 2);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(UUID.randomUUID()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid target");
        }

        @Test
        @DisplayName("throws when player target not allowed by spell effects")
        void throwsWhenPlayerTargetNotAllowed() {
            // Spell with no effects that allow both player + permanent targeting
            Card spell = new Card();
            spell.setName("Creature Only");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{R}");
            spell.setColor(CardColor.RED);
            spell.setMinTargets(1);
            spell.setMaxTargets(2);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(player2.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("This spell cannot target players");
        }

        @Test
        @DisplayName("passes with valid creature targets")
        void passesWithValidCreatureTargets() {
            Card spell = createMultiTargetSpell(1, 3);
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());
            Permanent p2 = addPermanent(player2.getId(), new GrizzlyBears());

            sut.validateMultiSpellTargets(gd, spell, List.of(p1.getId(), p2.getId()), player1.getId());
        }

        @Test
        @DisplayName("passes when targeting player with spell that allows it")
        void passesWhenTargetingPlayerWithAllowedSpell() {
            Card spell = createMultiTargetSpell(1, 2);

            sut.validateMultiSpellTargets(gd, spell, List.of(player2.getId()), player1.getId());
        }

        @Test
        @DisplayName("throws when target has shroud")
        void throwsWhenTargetHasShroud() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("throws when opponent's target has hexproof")
        void throwsWhenOpponentTargetHasHexproof() {
            Card spell = createMultiTargetSpell(1, 2);
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has hexproof and can't be targeted");
        }

        @Test
        @DisplayName("throws when target has protection from spell color")
        void throwsWhenTargetHasProtectionFromColor() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setColor(CardColor.BLACK);
            Permanent target = addPermanent(player2.getId(), new MirranCrusader());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has protection from black");
        }

        @Test
        @DisplayName("throws when target can't be targeted by spell color")
        void throwsWhenTargetCantBeTargetedBySpellColor() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setColor(CardColor.BLUE);
            Permanent target = addPermanent(player2.getId(), new KarplusanStrider());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
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
            Permanent target = addPermanent(player2.getId(), artifact);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("is not a creature");
        }

        @Test
        @DisplayName("throws when player target has shroud")
        void throwsWhenPlayerTargetHasShroud() {
            Card spell = createMultiTargetSpell(1, 2);
            addPermanent(player2.getId(), new TrueBeliever());

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(player2.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("has shroud and can't be targeted");
        }

        @Test
        @DisplayName("validates target filter when present")
        void validatesTargetFilterWhenPresent() {
            Card spell = createMultiTargetSpell(1, 2);
            spell.setTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"));
            Card artifact = new Card();
            artifact.setName("Test Artifact");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{1}");
            Permanent target = addPermanent(player2.getId(), artifact);

            assertThatThrownBy(() -> sut.validateMultiSpellTargets(gd, spell,
                    List.of(target.getId()), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Target must be a creature");
        }
    }

    // ===== isTargetIllegalOnResolution =====

    @Nested
    @DisplayName("isTargetIllegalOnResolution")
    class IsTargetIllegalOnResolution {

        @Test
        @DisplayName("returns false for non-targeting entry")
        void returnsFalseForNonTargetingEntry() {
            Card card = new GrizzlyBears();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1.getId(), "test", List.of());
            entry.setNonTargeting(true);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when target is still on battlefield")
        void returnsFalseWhenTargetStillOnBattlefield() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target left the battlefield")
        void returnsTrueWhenTargetLeftBattlefield() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, UUID.randomUUID(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when target is a player")
        void returnsFalseForPlayerTarget() {
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, player2.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target has shroud")
        void returnsTrueWhenTargetHasShroud() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Shrouded", CardColor.GREEN, Keyword.SHROUD));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns true when target has hexproof and is controlled by opponent")
        void returnsTrueWhenTargetHasHexproofAndControlledByOpponent() {
            Permanent target = addPermanent(player2.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when target has hexproof but is controlled by self")
        void returnsFalseWhenTargetHasHexproofButControlledBySelf() {
            Permanent target = addPermanent(player1.getId(),
                    createCreatureWithKeyword("Hexproof Guy", CardColor.GREEN, Keyword.HEXPROOF));
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when target has CantBeTargetOfSpellsOrAbilitiesEffect from opponent")
        void returnsTrueWhenTargetHasCantBeTargetEffect() {
            addPermanent(player2.getId(), new Asceticism());
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Burn", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Burn",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns true when target filter no longer matches")
        void returnsTrueWhenTargetFilterNoLongerMatches() {
            Card artifact = new Card();
            artifact.setName("Test Artifact");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{1}");
            Permanent target = addPermanent(player2.getId(), artifact);

            Card spell = createTargetingSpell("Destroy", CardColor.RED);
            spell.setTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Destroy",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("uses entry target filter over card target filter")
        void usesEntryTargetFilterOverCardFilter() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Spell", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Spell",
                    spell.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());
            // Entry has a filter that rejects creatures
            entry.setTargetFilter(new PermanentPredicateTargetFilter(
                    new PermanentIsArtifactPredicate(), "Target must be an artifact"));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Graveyard targets -----

        @Test
        @DisplayName("returns false when graveyard target is present")
        void returnsFalseWhenGraveyardTargetPresent() {
            Card cardInGY = new GrizzlyBears();
            gd.playerGraveyards.get(player2.getId()).add(cardInGY);

            Card spell = new Card();
            spell.setName("Raise Dead");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Raise Dead",
                    List.of(), cardInGY.getId(), Zone.GRAVEYARD);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when graveyard target is gone")
        void returnsTrueWhenGraveyardTargetGone() {
            Card spell = new Card();
            spell.setName("Raise Dead");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Raise Dead",
                    List.of(), UUID.randomUUID(), Zone.GRAVEYARD);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Stack targets -----

        @Test
        @DisplayName("returns false when stack target is still on stack")
        void returnsFalseWhenStackTargetPresent() {
            Card targetSpell = new GrizzlyBears();
            StackEntry targetEntry = new StackEntry(targetSpell, player2.getId());
            gd.stack.add(targetEntry);

            Card counterSpell = new Card();
            counterSpell.setName("Counter");
            counterSpell.setType(CardType.INSTANT);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, counterSpell, player1.getId(), "Counter",
                    List.of(), targetSpell.getId(), Zone.STACK);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns true when stack target has resolved")
        void returnsTrueWhenStackTargetGone() {
            Card counterSpell = new Card();
            counterSpell.setName("Counter");
            counterSpell.setType(CardType.INSTANT);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, counterSpell, player1.getId(), "Counter",
                    List.of(), UUID.randomUUID(), Zone.STACK);

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        // ----- Multi-target permanents -----

        @Test
        @DisplayName("returns true when all multi-target permanents are gone")
        void returnsTrueWhenAllMultiTargetPermanentsGone() {
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(UUID.randomUUID(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when some multi-target permanents remain")
        void returnsFalseWhenSomeMultiTargetPermanentsRemain() {
            Permanent p1 = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(p1.getId(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when multi-target includes a player")
        void returnsFalseWhenMultiTargetIncludesPlayer() {
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(player2.getId(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        // ----- Multi-target card IDs (graveyard) -----

        @Test
        @DisplayName("returns true when all multi-target card IDs are gone from graveyard")
        void returnsTrueWhenAllTargetCardIdsGone() {
            Card spell = new Card();
            spell.setName("Exile Cards");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Exile",
                    List.of(), List.of(UUID.randomUUID(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isTrue();
        }

        @Test
        @DisplayName("returns false when some multi-target card IDs remain in graveyard")
        void returnsFalseWhenSomeTargetCardIdsRemain() {
            Card cardInGY = new GrizzlyBears();
            gd.playerGraveyards.get(player2.getId()).add(cardInGY);

            Card spell = new Card();
            spell.setName("Exile Cards");
            spell.setType(CardType.SORCERY);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Exile",
                    List.of(), List.of(cardInGY.getId(), UUID.randomUUID()));

            assertThat(sut.isTargetIllegalOnResolution(gd, entry)).isFalse();
        }

        @Test
        @DisplayName("returns false when entry has no target at all")
        void returnsFalseWhenNoTargetAtAll() {
            Card card = new GrizzlyBears();
            StackEntry entry = new StackEntry(card, player1.getId());

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
            Card card = new GrizzlyBears();
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryTypeInPredicate when not matching")
        void rejectsTypeInPredicateWhenNotMatching() {
            Card card = new GrizzlyBears();
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryColorInPredicate")
        void matchesColorInPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryColorInPredicate(Set.of(CardColor.GREEN)), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryColorInPredicate when not matching")
        void rejectsColorInPredicateWhenNotMatching() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryColorInPredicate(Set.of(CardColor.RED)), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryIsSingleTargetPredicate with single target")
        void matchesSingleTargetPredicate() {
            Permanent target = addPermanent(player2.getId(), new GrizzlyBears());
            Card card = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, player1.getId(), "Bolt",
                    card.getEffects(EffectSlot.SPELL), 0, target.getId(), Map.of());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryIsSingleTargetPredicate(), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryIsSingleTargetPredicate when no target")
        void rejectsSingleTargetPredicateWhenNoTarget() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryIsSingleTargetPredicate(), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("StackEntryHasTargetPredicate always returns true")
        void hasTargetPredicateAlwaysReturnsTrue() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryHasTargetPredicate(), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("matches StackEntryManaValuePredicate")
        void matchesManaValuePredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            card.setManaCost("{1}{G}"); // mana value 2
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryManaValuePredicate(2), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryManaValuePredicate when not matching")
        void rejectsManaValuePredicateWhenNotMatching() {
            Card card = createCreature("Bear", CardColor.GREEN);
            card.setManaCost("{1}{G}"); // mana value 2
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryManaValuePredicate(3), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryTargetsYourPermanentPredicate when targeting your permanent")
        void matchesTargetsYourPermanentPredicate() {
            Permanent p1Creature = addPermanent(player1.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player2.getId(), "Bolt",
                    spell.getEffects(EffectSlot.SPELL), 0, p1Creature.getId(), Map.of());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects StackEntryTargetsYourPermanentPredicate when not targeting your permanent")
        void rejectsTargetsYourPermanentPredicateWhenNotTargetingYours() {
            Permanent p2Creature = addPermanent(player2.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Bolt", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1.getId(), "Bolt",
                    spell.getEffects(EffectSlot.SPELL), 0, p2Creature.getId(), Map.of());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches StackEntryTargetsYourPermanentPredicate via multi-target")
        void matchesTargetsYourPermanentPredicateViaMultiTarget() {
            Permanent p1Creature = addPermanent(player1.getId(), new GrizzlyBears());
            Card spell = createTargetingSpell("Multi", CardColor.RED);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, spell, player2.getId(), "Multi",
                    spell.getEffects(EffectSlot.SPELL), 0, List.of(p1Creature.getId()));

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryTargetsYourPermanentPredicate(), player1.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("matches AllOfPredicate when all inner predicates match")
        void matchesAllOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.GREEN))
                    )), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects AllOfPredicate when one inner predicate does not match")
        void rejectsAllOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.RED))
                    )), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches AnyOfPredicate when one inner predicate matches")
        void matchesAnyOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAnyOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.GREEN))
                    )), player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects AnyOfPredicate when no inner predicate matches")
        void rejectsAnyOfPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryAnyOfPredicate(List.of(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                            new StackEntryColorInPredicate(Set.of(CardColor.RED))
                    )), player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("matches NotPredicate when inner does not match")
        void matchesNotPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(CardColor.RED))),
                    player2.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejects NotPredicate when inner matches")
        void rejectsNotPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(CardColor.GREEN))),
                    player2.getId()))
                    .isFalse();
        }

        @Test
        @DisplayName("returns false for unknown predicate type")
        void returnsFalseForUnknownPredicate() {
            Card card = createCreature("Bear", CardColor.GREEN);
            StackEntry entry = new StackEntry(card, player1.getId());

            assertThat(sut.matchesStackEntryPredicate(gd, entry,
                    new UnknownPredicate(), player2.getId()))
                    .isFalse();
        }

        private record UnknownPredicate() implements com.github.laxika.magicalvibes.model.filter.StackEntryPredicate {}
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
}
