package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Focused unit tests for the declarative {@link TargetSpec} interpreter inside
 * {@link TargetValidationService} (refactor step 2). Drives it through the public
 * {@code checkEffectTargets} entry point with stub effects that declare a spec.
 */
@ExtendWith(MockitoExtension.class)
class TargetValidationServiceSpecTest {

    @Mock
    private GameQueryService gameQueryService;

    private TargetValidatorRegistry registry;
    private TargetValidationService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Card sourceCard;

    @BeforeEach
    void setUp() {
        registry = new TargetValidatorRegistry();
        // The interpreter composes the category restriction and the spec's narrowing predicate into
        // one PermanentPredicate and evaluates it for real, so predicate evaluation is the genuine
        // service over the mocked GameQueryService rather than a second mock.
        sut = new TargetValidationService(gameQueryService,
                new PredicateEvaluationService(gameQueryService), registry);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);

        sourceCard = new Card();
        sourceCard.setName("Bolt Source");
        sourceCard.setType(CardType.INSTANT);
        sourceCard.setColor(CardColor.RED);
        lenient().when(gameQueryService.getEffectiveCardColors(eq(gd), any(Card.class)))
                .thenAnswer(invocation -> effectiveColors(invocation.getArgument(1)));
    }

    private static Set<CardColor> effectiveColors(Card card) {
        Set<CardColor> colors = new HashSet<>(card.getColors());
        if (card.getColor() != null) {
            colors.add(card.getColor());
        }
        return colors;
    }

    // ===== stub effects declaring a spec =====

    private record CreatureHarmfulEffect() implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.harmful(TargetPredicates.creature());
        }
    }

    private record AnyTargetHarmfulEffect() implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.harmful(TargetPredicates.anyTarget());
        }
    }

    private record PermanentBenignWithPredicateEffect(PermanentPredicate predicate) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetPredicates.permanent(), predicate);
        }
    }

    private record CreatureBenignWithPredicateEffect(PermanentPredicate predicate) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetPredicates.creature(), predicate);
        }
    }

    private record LandBenignEffect() implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetPredicates.land());
        }
    }

    private record GraveyardCreatureEffect(GraveyardSearchScope scope) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetPredicates.graveyardCards(
                    new CardTypePredicate(CardType.CREATURE), scope));
        }
    }

    /** Default targetSpec() is NONE. */
    private record UntargetedEffect() implements CardEffect {
    }

    // ===== helpers =====

    private Permanent permanentOnBattlefield(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setColor(CardColor.GREEN);
        Permanent perm = new Permanent(card);
        when(gameQueryService.findPermanentById(gd, perm.getId())).thenReturn(perm);
        return perm;
    }

    private Optional<String> check(CardEffect effect, UUID targetId) {
        return sut.checkEffectTargets(List.of(effect),
                new TargetValidationContext(gd, targetId, null, sourceCard));
    }

    // ===== tests =====

    @Test
    @DisplayName("CREATURE spec rejects a land target")
    void creatureSpecRejectsLand() {
        Permanent land = permanentOnBattlefield("Forest", CardType.LAND);
        when(gameQueryService.isCreature(gd, land)).thenReturn(false);

        assertThat(check(new CreatureHarmfulEffect(), land.getId()))
                .contains("Target must be a creature");
    }

    @Test
    @DisplayName("ANY_TARGET spec accepts a player target")
    void anyTargetAcceptsPlayer() {
        assertThat(check(new AnyTargetHarmfulEffect(), player2Id)).isEmpty();
    }

    @Test
    @DisplayName("ANY_TARGET spec accepts a creature target")
    void anyTargetAcceptsCreature() {
        Permanent creature = permanentOnBattlefield("Grizzly Bears", CardType.CREATURE);
        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);

        assertThat(check(new AnyTargetHarmfulEffect(), creature.getId())).isEmpty();
    }

    @Test
    @DisplayName("ANY_TARGET spec rejects a land target")
    void anyTargetRejectsLand() {
        Permanent land = permanentOnBattlefield("Forest", CardType.LAND);
        when(gameQueryService.isCreature(gd, land)).thenReturn(false);

        assertThat(check(new AnyTargetHarmfulEffect(), land.getId()))
                .contains("Target must be a creature, planeswalker, battle, or player");
    }

    @Test
    @DisplayName("harmful spec runs the protection check on a permanent target")
    void harmfulSpecRunsProtection() {
        Permanent creature = permanentOnBattlefield("Pro-Red Bear", CardType.CREATURE);
        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        when(gameQueryService.hasProtectionFrom(gd, creature, CardColor.RED)).thenReturn(true);

        assertThat(check(new CreatureHarmfulEffect(), creature.getId()))
                .isPresent()
                .get().asString().contains("protection");
    }

    @Test
    @DisplayName("predicate narrowing rejects a permanent that does not match")
    void predicateNarrowingRejectsNonMatch() {
        Permanent perm = permanentOnBattlefield("Runeclaw Bear", CardType.CREATURE);

        assertThat(check(new PermanentBenignWithPredicateEffect(new PermanentIsArtifactPredicate()), perm.getId()))
                .contains("Target does not match the required predicate");
    }

    @Test
    @DisplayName("predicate narrowing accepts a permanent that matches")
    void predicateNarrowingAcceptsMatch() {
        Permanent perm = permanentOnBattlefield("Ornithopter", CardType.ARTIFACT);
        when(gameQueryService.isArtifact(gd, perm)).thenReturn(true);

        assertThat(check(new PermanentBenignWithPredicateEffect(new PermanentIsArtifactPredicate()), perm.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("a narrowed CREATURE spec still blames the creature half for a land")
    void narrowedCreatureSpecBlamesTheCreatureHalf() {
        Permanent land = permanentOnBattlefield("Forest", CardType.LAND);
        when(gameQueryService.isCreature(gd, land)).thenReturn(false);

        assertThat(check(new CreatureBenignWithPredicateEffect(new PermanentIsArtifactPredicate()), land.getId()))
                .contains("Target must be a creature");
    }

    @Test
    @DisplayName("LAND spec is layer-aware: a permanent turned into a land is a legal target")
    void landSpecIsLayerAware() {
        Permanent bears = permanentOnBattlefield("Grizzly Bears", CardType.CREATURE);
        when(gameQueryService.isLand(gd, bears)).thenReturn(true);

        assertThat(check(new LandBenignEffect(), bears.getId())).isEmpty();
    }

    @Test
    @DisplayName("NONE spec does nothing (no target checks) even with a null target")
    void noneSpecDoesNothing() {
        assertThat(check(new UntargetedEffect(), null)).isEmpty();
    }

    @Test
    @DisplayName("An omitted up-to-one graveyard target is legal")
    void optionalGraveyardTargetMayBeOmitted() {
        ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .upTo(true)
                .build();

        assertThat(sut.checkEffectTargets(
                List.of(effect),
                new TargetValidationContext(
                        gd, null, Zone.GRAVEYARD, sourceCard, 0, player1Id, null)))
                .isEmpty();
    }

    @Test
    @DisplayName("An omitted up-to-one graveyard target remains legal through a conditional wrapper")
    void optionalGraveyardTargetMayBeOmittedThroughConditionalEffect() {
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .upTo(true)
                .build();

        assertThat(sut.checkEffectTargets(
                List.of(new ConditionalEffect(new Morbid(), returnEffect)),
                new TargetValidationContext(
                        gd, null, Zone.GRAVEYARD, sourceCard, 0, player1Id, null)))
                .isEmpty();
    }

    @Test
    @DisplayName("GRAVEYARD_CARD spec rejects a card that does not match its predicate")
    void graveyardSpecRejectsPredicateMismatch() {
        Card land = new Card();
        land.setType(CardType.LAND);
        when(gameQueryService.canGraveyardCardsBeTargeted(gd)).thenReturn(true);
        when(gameQueryService.findCardInGraveyardById(gd, land.getId())).thenReturn(land);
        when(gameQueryService.findGraveyardOwnerById(gd, land.getId())).thenReturn(player1Id);

        assertThat(sut.checkEffectTargets(
                List.of(new GraveyardCreatureEffect(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new TargetValidationContext(
                        gd, land.getId(), Zone.GRAVEYARD, sourceCard, 0, player1Id, null)))
                .contains("Target card does not match the required predicate");
    }

    @Test
    @DisplayName("GRAVEYARD_CARD spec enforces the declared graveyard scope")
    void graveyardSpecRejectsCardOutsideScope() {
        Card creature = new Card();
        creature.setType(CardType.CREATURE);
        when(gameQueryService.canGraveyardCardsBeTargeted(gd)).thenReturn(true);
        when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);
        when(gameQueryService.findGraveyardOwnerById(gd, creature.getId())).thenReturn(player2Id);

        assertThat(sut.checkEffectTargets(
                List.of(new GraveyardCreatureEffect(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new TargetValidationContext(
                        gd, creature.getId(), Zone.GRAVEYARD, sourceCard, 0, player1Id, null)))
                .contains("Target card is not in an allowed graveyard");
    }

    @Test
    @DisplayName("spec runs first, then a kept class validator also runs")
    void specAndKeptClassValidatorBothRun() {
        // Register a class validator that vetoes with a distinct message; the spec passes
        // (valid creature), so a non-empty result here proves the validator ran after the spec.
        registry.register(CreatureHarmfulEffect.class,
                (ctx, effect) -> { throw new IllegalStateException("kept-validator veto"); });

        Permanent creature = permanentOnBattlefield("Grizzly Bears", CardType.CREATURE);
        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);

        assertThat(check(new CreatureHarmfulEffect(), creature.getId()))
                .contains("kept-validator veto");
    }
}
