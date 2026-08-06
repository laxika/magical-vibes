package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The safety net for the {@link TargetPredicate} targeting model (see
 * {@code agent-docs/TARGET_PREDICATE_PLAN.md}). It guards two things a single service test cannot:
 *
 * <ul>
 *   <li><b>The two evaluation paths agree.</b> A declared target is evaluated by
 *       {@link TargetPredicateEvaluationService} (target enumeration, may-ability enumeration) and,
 *       independently, by the spec interpreter inside {@link TargetValidationService} — which
 *       deliberately does not take the adapter, because injecting it would close a Spring
 *       constructor cycle. Two implementations of one restriction can drift, so every canonical
 *       declared target is driven through both against the same board and the answers compared.</li>
 *   <li><b>The declared targets admit exactly the kinds they claim to.</b> {@code admits(Kind)} is
 *       what the trigger collectors, {@code StepTriggerService} and the AI route on, so the
 *       kind-set of each named factory is pinned as an explicit table rather than derived.</li>
 * </ul>
 *
 * <p>The two type-<em>replacing</em> cases that were rules-incorrect before the migration (LAND, and
 * the planeswalker half of "any target") are pinned by their own tests at the bottom: both halves
 * must be layer-aware.</p>
 */
class TargetPredicateEquivalenceTest extends BaseCardTest {

    /**
     * Every declared target a {@code TargetPredicates} factory hands out, plus the {@code null}
     * "targets nothing" case. The sweeps below iterate this, so a new factory must be added here.
     */
    private static final Map<String, TargetPredicate> CANONICAL_TARGETS = canonicalTargets();

    /** The kinds each canonical target admits — the contract {@code TargetSpec.admits} answers. */
    private static final Map<String, Set<TargetPredicate.Kind>> ADMITTED_KINDS = admittedKinds();

    private TargetPredicateEvaluationService sut;
    private TargetValidationService targetValidationService;

    private Card sourceCard;
    private Map<String, Permanent> permanents;
    private Card ownGraveyardCard;
    private Card opponentGraveyardCard;
    private Card exiledCard;
    private StackEntry spellOnStack;

    @BeforeEach
    void wireAndBuildBoard() {
        sut = GameTestEngineContext.get().getBean(TargetPredicateEvaluationService.class);
        targetValidationService = GameTestEngineContext.get().getBean(TargetValidationService.class);

        sourceCard = card("Equivalence Source", CardType.INSTANT);

        permanents = new LinkedHashMap<>();
        permanents.put("creature", battlefield(player1, card("Bear", CardType.CREATURE)));
        permanents.put("land", battlefield(player1, card("Forest", CardType.LAND)));
        permanents.put("planeswalker", battlefield(player1, card("Walker", CardType.PLANESWALKER)));
        permanents.put("artifact", battlefield(player2, card("Thopter", CardType.ARTIFACT)));
        permanents.put("enchantment", battlefield(player2, card("Pacifism", CardType.ENCHANTMENT)));

        Card landCreatureCard = card("Dryad Arbor", CardType.LAND);
        landCreatureCard.setAdditionalTypes(Set.of(CardType.CREATURE));
        permanents.put("landCreature", battlefield(player2, landCreatureCard));

        Permanent animatedLand = battlefield(player1, card("Mutavault", CardType.LAND));
        animatedLand.setAnimatedUntilEndOfTurn(true);
        permanents.put("animatedLand", animatedLand);

        ownGraveyardCard = card("Own Graveyard Card", CardType.SORCERY);
        opponentGraveyardCard = card("Opponent Graveyard Card", CardType.SORCERY);
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));

        exiledCard = card("Exiled Card", CardType.SORCERY);
        harness.setExile(player1, List.of(exiledCard));

        spellOnStack = new StackEntry(card("Stack Spell", CardType.INSTANT), player2.getId());
    }

    @Test
    @DisplayName("The evaluator and the spec interpreter accept exactly the same permanents")
    void permanentCandidatesAgreeWithTheSpecInterpreter() {
        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            for (Map.Entry<String, Permanent> candidate : permanents.entrySet()) {
                boolean interpreter = TargetSpec.benign(declared.getValue()).admits(TargetPredicate.Kind.PERMANENT)
                        && specInterpreterAccepts(declared.getValue(), candidate.getValue().getId());
                boolean evaluator = matchesPermanent(declared.getValue(), candidate.getValue());
                if (interpreter != evaluator) {
                    mismatches.add(declared.getKey() + " on permanent '" + candidate.getKey()
                            + "': interpreter=" + interpreter + " evaluator=" + evaluator);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("The evaluator and the spec interpreter accept exactly the same players")
    void playerCandidatesAgreeWithTheSpecInterpreter() {
        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            for (UUID playerId : List.of(player1.getId(), player2.getId())) {
                boolean interpreter = TargetSpec.benign(declared.getValue()).admits(TargetPredicate.Kind.PLAYER)
                        && specInterpreterAccepts(declared.getValue(), playerId);
                boolean evaluator = matchesPlayer(declared.getValue(), playerId);
                if (interpreter != evaluator) {
                    mismatches.add(declared.getKey() + " on player " + (playerId.equals(player1.getId())
                            ? "self" : "opponent") + ": interpreter=" + interpreter + " evaluator=" + evaluator);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("Only a graveyard target accepts a graveyard card, and only within its declared scope")
    void graveyardCandidatesMatchTheDeclaredScope() {
        List<String> mismatches = new ArrayList<>();
        Map<Card, UUID> candidates = Map.of(
                ownGraveyardCard, player1.getId(),
                opponentGraveyardCard, player2.getId());
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            for (Map.Entry<Card, UUID> candidate : candidates.entrySet()) {
                boolean expected = inDeclaredScope(declared.getValue(), candidate.getValue());
                boolean actual = matchesGraveyardCard(
                        declared.getValue(), candidate.getKey(), candidate.getValue());
                if (expected != actual) {
                    mismatches.add(declared.getKey() + " on " + candidate.getKey().getName()
                            + ": scope=" + expected + " evaluator=" + actual);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("Only exileCard() accepts an exiled card, and it accepts every one")
    void exileCandidatesMatchTheAdmittedKind() {
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            TargetPredicate predicate = declared.getValue();
            boolean actual = predicate != null
                    && sut.matchesExiledCard(predicate, exiledCard, filterContext());
            assertThat(actual)
                    .as("%s on an exiled card", declared.getKey())
                    .isEqualTo(TargetSpec.benign(predicate).admits(TargetPredicate.Kind.EXILED_CARD));
        }
    }

    @Test
    @DisplayName("Only spellOnStack() accepts a spell on the stack, and it accepts every one")
    void stackCandidatesMatchTheAdmittedKind() {
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            TargetPredicate predicate = declared.getValue();
            boolean actual = predicate != null
                    && sut.matchesSpell(predicate, spellOnStack, player1.getId(), null, filterContext());
            assertThat(actual)
                    .as("%s on a spell on the stack", declared.getKey())
                    .isEqualTo(TargetSpec.benign(predicate).admits(TargetPredicate.Kind.SPELL));
        }
    }

    @Test
    @DisplayName("creature() and land() stay layer-aware for an animated land: it is both")
    void animatedLandIsBothCreatureAndLand() {
        Permanent animatedLand = permanents.get("animatedLand");
        assertThat(gqs.isCreature(gd, animatedLand)).isTrue();
        assertThat(gqs.isLand(gd, animatedLand)).isTrue();

        assertThat(matchesPermanent(TargetPredicates.creature(), animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetPredicates.land(), animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetPredicates.creatureOrPlaneswalker(), animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetPredicates.anyTarget(), animatedLand)).isTrue();

        Permanent plainLand = permanents.get("land");
        assertThat(matchesPermanent(TargetPredicates.creature(), plainLand)).isFalse();
        assertThat(matchesPermanent(TargetPredicates.land(), plainLand)).isTrue();
        assertThat(matchesPermanent(TargetPredicates.anyTarget(), plainLand)).isFalse();
    }

    @Test
    @DisplayName("A spec's narrowing predicate becomes a narrowing of the permanent leaf")
    void narrowingPredicateNarrowsThePermanentLeaf() {
        Permanent artifact = permanents.get("artifact");
        Permanent creature = permanents.get("creature");

        TargetPredicate narrowed = TargetSpec
                .benign(TargetPredicates.permanent(), new PermanentIsArtifactPredicate())
                .targetPredicate();

        assertThat(narrowed).isEqualTo(TargetPredicates.permanents(new PermanentIsArtifactPredicate()));
        assertThat(sut.matchesPermanent(narrowed, artifact, filterContext())).isTrue();
        assertThat(sut.matchesPermanent(narrowed, creature, filterContext())).isFalse();
    }

    @Test
    @DisplayName("A NONE spec has no target predicate at all")
    void noneSpecHasNoTargetPredicate() {
        assertThat(TargetSpec.NONE.targetPredicate()).isNull();
        assertThat(TargetSpec.NONE.declaredTarget()).isNull();
        assertThat(TargetSpec.NONE.declares(TargetPredicates.creature())).isFalse();
    }

    @Test
    @DisplayName("A narrowing predicate stays on predicate() and does not fold into declaredTarget()")
    void narrowingStaysOffTheDeclaredTarget() {
        TargetSpec spec = TargetSpec.benign(TargetPredicates.creature(), new PermanentIsArtifactPredicate());

        assertThat(spec.declaredTarget()).isEqualTo(TargetPredicates.creature());
        assertThat(spec.declares(TargetPredicates.creature())).isTrue();
        assertThat(spec.predicate()).isEqualTo(new PermanentIsArtifactPredicate());
    }

    @Test
    @DisplayName("Every canonical declared target admits exactly the kinds it claims")
    void everyDeclaredTargetAdmitsExactlyItsKinds() {
        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            TargetSpec spec = TargetSpec.benign(declared.getValue());
            Set<TargetPredicate.Kind> expected = ADMITTED_KINDS.get(declared.getKey());
            for (TargetPredicate.Kind kind : TargetPredicate.Kind.values()) {
                if (spec.admits(kind) != expected.contains(kind)) {
                    mismatches.add(declared.getKey() + " admits(" + kind + ")=" + spec.admits(kind)
                            + " but the declared kind set is " + expected);
                }
            }
        }
        assertThat(mismatches).isEmpty();
        assertThat(ADMITTED_KINDS.keySet()).isEqualTo(CANONICAL_TARGETS.keySet());
    }

    @Test
    @DisplayName("A narrowing predicate cannot add or remove a kind, so admits(Kind) ignores it")
    void narrowingDoesNotDisturbWhichKindsAreAdmitted() {
        for (Map.Entry<String, TargetPredicate> declared : CANONICAL_TARGETS.entrySet()) {
            TargetSpec bare = TargetSpec.benign(declared.getValue());
            TargetSpec narrowed = TargetSpec.benign(declared.getValue(), new PermanentIsArtifactPredicate());

            for (TargetPredicate.Kind kind : TargetPredicate.Kind.values()) {
                assertThat(narrowed.admits(kind))
                        .as("%s narrowed to artifacts still admits %s the same way", declared.getKey(), kind)
                        .isEqualTo(bare.admits(kind));
            }
            assertThat(narrowed.graveyardScope())
                    .as("narrowing does not disturb %s's graveyard scope", declared.getKey())
                    .isEqualTo(bare.graveyardScope());
        }
    }

    @Test
    @DisplayName("anyTarget() and playerOrPermanent() are structurally distinct")
    void anyTargetIsNotPlayerOrPermanent() {
        assertThat(TargetPredicates.anyTarget()).isNotEqualTo(TargetPredicates.playerOrPermanent());

        Permanent artifact = permanents.get("artifact");
        assertThat(matchesPermanent(TargetPredicates.playerOrPermanent(), artifact)).isTrue();
        assertThat(matchesPermanent(TargetPredicates.anyTarget(), artifact)).isFalse();
        assertThat(matchesPlayer(TargetPredicates.playerOrPermanent(), player2.getId())).isTrue();
        assertThat(matchesPlayer(TargetPredicates.anyTarget(), player2.getId())).isTrue();
    }

    /**
     * {@code declares} is the identity test a reader must use when it means one specific
     * declaration. Asking "does it admit players and permanents?" instead is exactly the lossy test
     * that made "any target" indistinguishable from "a player or any permanent".
     */
    @Test
    @DisplayName("declares() tells anyTarget() and playerOrPermanent() apart; admits() cannot")
    void declaresDistinguishesWhatAdmitsCannot() {
        TargetSpec anyTarget = TargetSpec.harmful(TargetPredicates.anyTarget());
        TargetSpec playerOrPermanent = TargetSpec.benign(TargetPredicates.playerOrPermanent());

        assertThat(anyTarget.declares(TargetPredicates.anyTarget())).isTrue();
        assertThat(playerOrPermanent.declares(TargetPredicates.anyTarget())).isFalse();

        for (TargetSpec spec : List.of(anyTarget, playerOrPermanent)) {
            assertThat(spec.admits(TargetPredicate.Kind.PLAYER)).isTrue();
            assertThat(spec.admits(TargetPredicate.Kind.PERMANENT)).isTrue();
        }
    }

    /**
     * A declared target no named factory produces is now a legal thing to build — the transitional
     * bridge that rejected one (there was no enum constant for "an artifact or a player") is gone
     * with the enum. Both evaluation paths must handle it.
     */
    @Test
    @DisplayName("A hand-composed cross-kind target works in both evaluation paths")
    void aHandComposedCrossKindTargetIsEvaluatedByBothPaths() {
        TargetPredicate artifactOrPlayer = TargetPredicates.anyOf(
                TargetPredicates.player(), TargetPredicates.permanents(new PermanentIsArtifactPredicate()));

        Permanent artifact = permanents.get("artifact");
        Permanent creature = permanents.get("creature");

        assertThat(matchesPermanent(artifactOrPlayer, artifact)).isTrue();
        assertThat(matchesPermanent(artifactOrPlayer, creature)).isFalse();
        assertThat(matchesPlayer(artifactOrPlayer, player2.getId())).isTrue();

        assertThat(specInterpreterAccepts(artifactOrPlayer, artifact.getId())).isTrue();
        assertThat(specInterpreterAccepts(artifactOrPlayer, creature.getId())).isFalse();
        assertThat(specInterpreterAccepts(artifactOrPlayer, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("AnyOf flattens nested disjunctions and orders leaves canonically")
    void anyOfIsCanonical() {
        TargetPredicate players = TargetPredicates.player();
        TargetPredicate perms = TargetPredicates.permanent();
        TargetPredicate spells = TargetPredicates.spellOnStack();

        TargetPredicate nested = TargetPredicates.anyOf(spells, TargetPredicates.anyOf(players, perms));
        TargetPredicate flat = TargetPredicates.anyOf(perms, players, spells);

        assertThat(nested).isEqualTo(flat);
        assertThat(((TargetPredicate.AnyOf) flat).options()).containsExactly(perms, players, spells);
    }

    @Test
    @DisplayName("AnyOf rejects two leaves of the same kind and a degenerate single option")
    void anyOfRejectsUnsoundShapes() {
        TargetPredicate creatures = TargetPredicates.creature();
        TargetPredicate lands = TargetPredicates.land();

        assertThatThrownBy(() -> new TargetPredicate.AnyOf(creatures, lands))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at most one leaf per kind");

        assertThatThrownBy(() -> new TargetPredicate.AnyOf(creatures))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least two leaves");

        // The factory is forgiving where the shape is still sound: one option is just that option.
        assertThat(TargetPredicates.anyOf(creatures)).isEqualTo(creatures);
    }

    @Test
    @DisplayName("A leaf never carries a null inner predicate")
    void leavesRejectNullInnerPredicates() {
        assertThatThrownBy(() -> new TargetPredicate.Permanents(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetPredicate.Players(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetPredicate.Spells(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetPredicate.ExiledCards(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetPredicate.GraveyardCards(null, GraveyardSearchScope.ALL_GRAVEYARDS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Evaluating a permanent without game data is rejected, not silently un-layered")
    void permanentEvaluationRequiresGameData() {
        Permanent animatedLand = permanents.get("animatedLand");
        assertThatThrownBy(() -> sut.matchesPermanent(
                TargetPredicates.creature(), animatedLand, FilterContext.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GameData");
    }

    @Test
    @DisplayName("The kind leaves are exactly the five factories build, with the mapped inner predicates")
    void factoriesProduceTheMappedShapes() {
        assertThat(TargetPredicates.player())
                .isEqualTo(TargetPredicates.players(new PlayerRelationPredicate(PlayerRelation.ANY)));
        assertThat(TargetPredicates.permanent())
                .isEqualTo(TargetPredicates.permanents(new PermanentTruePredicate()));
        assertThat(TargetPredicates.spellOnStack())
                .isEqualTo(TargetPredicates.spells(new StackEntryTruePredicate()));
        for (GraveyardSearchScope scope : GraveyardSearchScope.values()) {
            assertThat(TargetPredicates.graveyardCard(scope).graveyardScope())
                    .as("graveyardCard(%s) carries its scope on the leaf", scope)
                    .contains(scope);
        }
    }

    /**
     * {@code land()} used to read the <em>printed</em> card type, so a permanent that a
     * type-<em>replacing</em> effect turned into a land (CR 613.1d, layer 4) was not a legal
     * "target land". Step 2 adopted the layer-aware answer, so both halves now agree that it is.
     */
    @Test
    @DisplayName("land() is layer-aware in both halves for a permanent turned into a land")
    void landIsLayerAwareForATypeReplacedPermanent() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(bears.getCard().hasType(CardType.LAND)).isFalse();

        assertThat(specInterpreterAccepts(TargetPredicates.land(), bears.getId())).isTrue();
        assertThat(matchesPermanent(TargetPredicates.land(), bears)).isTrue();

        assertThat(specInterpreterAccepts(TargetPredicates.creature(), bears.getId())).isFalse();
        assertThat(matchesPermanent(TargetPredicates.creature(), bears)).isFalse();
    }

    /**
     * The mirror case: {@code anyTarget()} used to read the printed planeswalker type, so a
     * planeswalker that stopped being one still passed. Step 2 adopted the layer-aware answer —
     * CR 115.4 lists what "any target" may be, judged after layer 4.
     */
    @Test
    @DisplayName("anyTarget() is layer-aware in both halves for a de-typed planeswalker")
    void anyTargetIsLayerAwareForATypeReplacedPlaneswalker() {
        Permanent walker = permanents.get("planeswalker");
        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(walker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isPlaneswalker(gd, walker)).isFalse();
        assertThat(walker.getCard().hasType(CardType.PLANESWALKER)).isTrue();

        assertThat(specInterpreterAccepts(TargetPredicates.anyTarget(), walker.getId())).isFalse();
        assertThat(matchesPermanent(TargetPredicates.anyTarget(), walker)).isFalse();
    }

    private static Map<String, TargetPredicate> canonicalTargets() {
        Map<String, TargetPredicate> targets = new LinkedHashMap<>();
        targets.put("targets nothing", null);
        targets.put("player()", TargetPredicates.player());
        targets.put("permanent()", TargetPredicates.permanent());
        targets.put("creature()", TargetPredicates.creature());
        targets.put("land()", TargetPredicates.land());
        targets.put("creatureOrPlaneswalker()", TargetPredicates.creatureOrPlaneswalker());
        targets.put("playerOrPermanent()", TargetPredicates.playerOrPermanent());
        targets.put("playerOrPlaneswalker()", TargetPredicates.playerOrPlaneswalker());
        targets.put("anyTarget()", TargetPredicates.anyTarget());
        targets.put("spellOnStack()", TargetPredicates.spellOnStack());
        for (GraveyardSearchScope scope : GraveyardSearchScope.values()) {
            targets.put("graveyardCard(" + scope + ")", TargetPredicates.graveyardCard(scope));
        }
        targets.put("exileCard()", TargetPredicates.exileCard());
        // Not Map.copyOf: the "targets nothing" entry is a null value, which it rejects.
        return Collections.unmodifiableMap(targets);
    }

    private static Map<String, Set<TargetPredicate.Kind>> admittedKinds() {
        Set<TargetPredicate.Kind> permanentOnly = EnumSet.of(TargetPredicate.Kind.PERMANENT);
        Set<TargetPredicate.Kind> playerAndPermanent =
                EnumSet.of(TargetPredicate.Kind.PLAYER, TargetPredicate.Kind.PERMANENT);

        Map<String, Set<TargetPredicate.Kind>> kinds = new LinkedHashMap<>();
        kinds.put("targets nothing", EnumSet.noneOf(TargetPredicate.Kind.class));
        kinds.put("player()", EnumSet.of(TargetPredicate.Kind.PLAYER));
        kinds.put("permanent()", permanentOnly);
        kinds.put("creature()", permanentOnly);
        kinds.put("land()", permanentOnly);
        kinds.put("creatureOrPlaneswalker()", permanentOnly);
        kinds.put("playerOrPermanent()", playerAndPermanent);
        kinds.put("playerOrPlaneswalker()", playerAndPermanent);
        kinds.put("anyTarget()", playerAndPermanent);
        kinds.put("spellOnStack()", EnumSet.of(TargetPredicate.Kind.SPELL));
        for (GraveyardSearchScope scope : GraveyardSearchScope.values()) {
            kinds.put("graveyardCard(" + scope + ")", EnumSet.of(TargetPredicate.Kind.GRAVEYARD_CARD));
        }
        kinds.put("exileCard()", EnumSet.of(TargetPredicate.Kind.EXILED_CARD));
        return Map.copyOf(kinds);
    }

    private boolean matchesPermanent(TargetPredicate declaredTarget, Permanent permanent) {
        return declaredTarget != null && sut.matchesPermanent(declaredTarget, permanent, filterContext());
    }

    private boolean matchesPlayer(TargetPredicate declaredTarget, UUID playerId) {
        return declaredTarget != null && sut.matchesPlayer(declaredTarget, playerId, player1.getId(), gd);
    }

    private boolean matchesGraveyardCard(TargetPredicate declaredTarget, Card card, UUID graveyardOwnerId) {
        return declaredTarget != null
                && sut.matchesGraveyardCard(declaredTarget, card, graveyardOwnerId, player1.getId(), filterContext());
    }

    /**
     * Whether the real {@code TargetSpec} interpreter accepts {@code targetId} for a benign spec
     * declaring {@code declaredTarget}. Benign so the CR 702.16b protection check (an orthogonal
     * axis the predicate deliberately does not carry) never fires.
     */
    private boolean specInterpreterAccepts(TargetPredicate declaredTarget, UUID targetId) {
        return targetValidationService.checkEffectTargets(
                List.of(new DeclaredTargetStubEffect(declaredTarget)),
                new TargetValidationContext(gd, targetId, null, sourceCard)).isEmpty();
    }

    /** Whether {@code declaredTarget}'s graveyard scope reaches {@code graveyardOwnerId}'s graveyard. */
    private boolean inDeclaredScope(TargetPredicate declaredTarget, UUID graveyardOwnerId) {
        return TargetSpec.benign(declaredTarget).graveyardScope()
                .map(scope -> scope.graveyardOwners(gd.orderedPlayerIds, player1.getId()))
                .orElse(List.of())
                .contains(graveyardOwnerId);
    }

    private FilterContext filterContext() {
        return FilterContext.of(gd)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(player1.getId());
    }

    private Permanent battlefield(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setColor(CardColor.GREEN);
        return card;
    }

    /** Declares nothing but the target under test, so the interpreter is exercised in isolation. */
    private record DeclaredTargetStubEffect(TargetPredicate declaredTarget) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(declaredTarget);
        }
    }
}
