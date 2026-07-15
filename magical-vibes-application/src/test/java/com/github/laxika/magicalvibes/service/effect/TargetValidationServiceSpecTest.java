package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    private TargetValidatorRegistry registry;
    private TargetValidationService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Card sourceCard;

    @BeforeEach
    void setUp() {
        registry = new TargetValidatorRegistry();
        sut = new TargetValidationService(gameQueryService, predicateEvaluationService, registry);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);

        sourceCard = new Card();
        sourceCard.setName("Bolt Source");
        sourceCard.setType(CardType.INSTANT);
        sourceCard.setColor(CardColor.RED);
    }

    // ===== stub effects declaring a spec =====

    private record CreatureHarmfulEffect() implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.harmful(TargetCategory.CREATURE);
        }
    }

    private record AnyTargetHarmfulEffect() implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.harmful(TargetCategory.ANY_TARGET);
        }
    }

    private record PermanentBenignWithPredicateEffect(PermanentPredicate predicate) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetCategory.PERMANENT, predicate);
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
                .contains("Target must be a creature, planeswalker, or player");
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
        PermanentPredicate predicate = new PermanentIsArtifactPredicate();
        when(predicateEvaluationService.matchesPermanentPredicate(gd, perm, predicate)).thenReturn(false);

        assertThat(check(new PermanentBenignWithPredicateEffect(predicate), perm.getId()))
                .contains("Target does not match the required predicate");
    }

    @Test
    @DisplayName("predicate narrowing accepts a permanent that matches")
    void predicateNarrowingAcceptsMatch() {
        Permanent perm = permanentOnBattlefield("Ornithopter", CardType.ARTIFACT);
        PermanentPredicate predicate = new PermanentIsArtifactPredicate();
        when(predicateEvaluationService.matchesPermanentPredicate(gd, perm, predicate)).thenReturn(true);

        assertThat(check(new PermanentBenignWithPredicateEffect(predicate), perm.getId())).isEmpty();
    }

    @Test
    @DisplayName("NONE spec does nothing (no target checks) even with a null target")
    void noneSpecDoesNothing() {
        assertThat(check(new UntargetedEffect(), null)).isEmpty();
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
