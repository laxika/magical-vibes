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
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The safety net for the {@code TargetCategory} → {@link TargetPredicate} migration
 * (see {@code agent-docs/TARGET_PREDICATE_PLAN.md}): for every {@link TargetCategory} constant, the
 * predicate {@link TargetPredicates#forCategory} produces must accept and reject exactly the
 * candidates the category accepts and rejects today.
 *
 * <p>"What the category does today" is reconstructed from the two halves the engine actually uses,
 * not from a re-reading of the enum's javadoc:</p>
 *
 * <ul>
 *   <li><b>Admission</b> — which kinds of candidate the category offers at all. This is
 *       {@code includesPermanents()} / {@code includesPlayers()} / {@code isGraveyard()} plus the
 *       two identity comparisons, exactly as {@code EffectResolution.collectTargetTypes} and
 *       {@code ValidTargetService} read them.</li>
 *   <li><b>Narrowing</b> — for an admitted permanent or player, whether the real
 *       {@link TargetValidationService} spec interpreter accepts it. Driven through the public
 *       {@code checkEffectTargets} entry point with a stub effect, against a real board, so the
 *       layer-aware creature check is the production one.</li>
 * </ul>
 *
 * <p>Since Step 2 the interpreter <em>is</em> the predicate, so the sweep now guards the admission
 * axis — which kinds a category offers at all. The two type-<em>replacing</em> cases that used to
 * diverge (LAND, and the planeswalker half of ANY_TARGET) were adopted in the layer-aware direction
 * and are pinned by their own tests at the bottom.</p>
 */
class TargetPredicateEquivalenceTest extends BaseCardTest {

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
    @DisplayName("Every category's predicate accepts exactly the permanents the category accepts today")
    void permanentCandidatesMatchCategory() {
        List<String> mismatches = new ArrayList<>();
        for (TargetCategory category : TargetCategory.values()) {
            for (Map.Entry<String, Permanent> candidate : permanents.entrySet()) {
                boolean expected = category.includesPermanents()
                        && specInterpreterAccepts(category, candidate.getValue().getId());
                boolean actual = matchesPermanent(category, candidate.getValue());
                if (expected != actual) {
                    mismatches.add(category + " on permanent '" + candidate.getKey()
                            + "': category=" + expected + " predicate=" + actual);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("Every category's predicate accepts exactly the players the category accepts today")
    void playerCandidatesMatchCategory() {
        List<String> mismatches = new ArrayList<>();
        for (TargetCategory category : TargetCategory.values()) {
            for (UUID playerId : List.of(player1.getId(), player2.getId())) {
                boolean expected = category.includesPlayers()
                        && specInterpreterAccepts(category, playerId);
                boolean actual = matchesPlayer(category, playerId);
                if (expected != actual) {
                    mismatches.add(category + " on player " + (playerId.equals(player1.getId())
                            ? "self" : "opponent") + ": category=" + expected + " predicate=" + actual);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("Every category's predicate accepts exactly the graveyard cards the category scopes to today")
    void graveyardCandidatesMatchCategory() {
        List<String> mismatches = new ArrayList<>();
        for (TargetCategory category : TargetCategory.values()) {
            Map<Card, UUID> candidates = Map.of(
                    ownGraveyardCard, player1.getId(),
                    opponentGraveyardCard, player2.getId());
            for (Map.Entry<Card, UUID> candidate : candidates.entrySet()) {
                boolean expected = category.isGraveyard()
                        && inScopeToday(category, candidate.getValue());
                boolean actual = matchesGraveyardCard(category, candidate.getKey(), candidate.getValue());
                if (expected != actual) {
                    mismatches.add(category + " on " + candidate.getKey().getName()
                            + ": category=" + expected + " predicate=" + actual);
                }
            }
        }
        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("Only EXILE_CARD's predicate accepts an exiled card, and it accepts every one")
    void exileCandidatesMatchCategory() {
        for (TargetCategory category : TargetCategory.values()) {
            TargetPredicate predicate = TargetPredicates.forCategory(category);
            boolean actual = predicate != null
                    && sut.matchesExiledCard(predicate, exiledCard, filterContext());
            assertThat(actual)
                    .as("%s on an exiled card", category)
                    .isEqualTo(category == TargetCategory.EXILE_CARD);
        }
    }

    @Test
    @DisplayName("Only SPELL_ON_STACK's predicate accepts a spell on the stack, and it accepts every one")
    void stackCandidatesMatchCategory() {
        for (TargetCategory category : TargetCategory.values()) {
            TargetPredicate predicate = TargetPredicates.forCategory(category);
            boolean actual = predicate != null
                    && sut.matchesSpell(predicate, spellOnStack, player1.getId(), null, filterContext());
            assertThat(actual)
                    .as("%s on a spell on the stack", category)
                    .isEqualTo(category == TargetCategory.SPELL_ON_STACK);
        }
    }

    @Test
    @DisplayName("CREATURE and LAND stay layer-aware for an animated land: it is both")
    void animatedLandIsBothCreatureAndLand() {
        Permanent animatedLand = permanents.get("animatedLand");
        assertThat(gqs.isCreature(gd, animatedLand)).isTrue();
        assertThat(gqs.isLand(gd, animatedLand)).isTrue();

        assertThat(matchesPermanent(TargetCategory.CREATURE, animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetCategory.LAND, animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetCategory.CREATURE_OR_PLANESWALKER, animatedLand)).isTrue();
        assertThat(matchesPermanent(TargetCategory.ANY_TARGET, animatedLand)).isTrue();

        Permanent plainLand = permanents.get("land");
        assertThat(matchesPermanent(TargetCategory.CREATURE, plainLand)).isFalse();
        assertThat(matchesPermanent(TargetCategory.LAND, plainLand)).isTrue();
        assertThat(matchesPermanent(TargetCategory.ANY_TARGET, plainLand)).isFalse();
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
        assertThat(TargetPredicates.forCategory(TargetCategory.NONE)).isNull();
    }

    @Test
    @DisplayName("Every category round-trips through the declared target a spec now stores")
    void everyCategoryRoundTripsThroughTheDeclaredTarget() {
        for (TargetCategory category : TargetCategory.values()) {
            assertThat(TargetSpec.benign(TargetPredicates.forCategory(category)).category())
                    .as("round-trip of %s", category)
                    .isEqualTo(category);
        }
    }

    @Test
    @DisplayName("A narrowing predicate does not disturb the category a spec reports")
    void narrowingDoesNotDisturbTheReportedCategory() {
        TargetSpec spec = TargetSpec.benign(TargetPredicates.creature(), new PermanentIsArtifactPredicate());

        assertThat(spec.category()).isEqualTo(TargetCategory.CREATURE);
        assertThat(spec.predicate()).isEqualTo(new PermanentIsArtifactPredicate());
    }

    @Test
    @DisplayName("A declared target no category can express fails loudly rather than being rounded")
    void anUnmappableDeclaredTargetThrows() {
        TargetPredicate artifactOrPlayer = TargetPredicates.anyOf(
                TargetPredicates.player(), TargetPredicates.permanents(new PermanentIsArtifactPredicate()));

        assertThatThrownBy(() -> TargetSpec.benign(artifactOrPlayer).category())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No TargetCategory expresses");
    }

    @Test
    @DisplayName("ANY_TARGET and PLAYER_OR_PERMANENT are structurally distinct")
    void anyTargetIsNotPlayerOrPermanent() {
        assertThat(TargetPredicates.anyTarget()).isNotEqualTo(TargetPredicates.playerOrPermanent());

        Permanent artifact = permanents.get("artifact");
        assertThat(matchesPermanent(TargetCategory.PLAYER_OR_PERMANENT, artifact)).isTrue();
        assertThat(matchesPermanent(TargetCategory.ANY_TARGET, artifact)).isFalse();
        assertThat(matchesPlayer(TargetCategory.PLAYER_OR_PERMANENT, player2.getId())).isTrue();
        assertThat(matchesPlayer(TargetCategory.ANY_TARGET, player2.getId())).isTrue();
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
        assertThat(TargetPredicates.graveyardCard().leaf(TargetPredicate.Kind.GRAVEYARD_CARD))
                .get()
                .extracting(leaf -> ((TargetPredicate.GraveyardCards) leaf).scope())
                .isEqualTo(GraveyardSearchScope.OPPONENT_GRAVEYARD);
        assertThat(TargetPredicates.anyGraveyardCard().leaf(TargetPredicate.Kind.GRAVEYARD_CARD))
                .get()
                .extracting(leaf -> ((TargetPredicate.GraveyardCards) leaf).scope())
                .isEqualTo(GraveyardSearchScope.ALL_GRAVEYARDS);
        assertThat(TargetPredicates.controllersGraveyardCard().leaf(TargetPredicate.Kind.GRAVEYARD_CARD))
                .get()
                .extracting(leaf -> ((TargetPredicate.GraveyardCards) leaf).scope())
                .isEqualTo(GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /**
     * {@code LAND} used to read the <em>printed</em> card type, so a permanent that a
     * type-<em>replacing</em> effect turned into a land (CR 613.1d, layer 4) was not a legal
     * "target land". Step 2 adopted the layer-aware answer, so both halves now agree that it is.
     */
    @Test
    @DisplayName("LAND is layer-aware in both halves for a permanent turned into a land")
    void landIsLayerAwareForATypeReplacedPermanent() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(bears.getCard().hasType(CardType.LAND)).isFalse();

        assertThat(specInterpreterAccepts(TargetCategory.LAND, bears.getId())).isTrue();
        assertThat(matchesPermanent(TargetCategory.LAND, bears)).isTrue();

        assertThat(specInterpreterAccepts(TargetCategory.CREATURE, bears.getId())).isFalse();
        assertThat(matchesPermanent(TargetCategory.CREATURE, bears)).isFalse();
    }

    /**
     * The mirror case: {@code ANY_TARGET} used to read the printed planeswalker type, so a
     * planeswalker that stopped being one still passed. Step 2 adopted the layer-aware answer —
     * CR 115.4 lists what "any target" may be, judged after layer 4.
     */
    @Test
    @DisplayName("ANY_TARGET is layer-aware in both halves for a de-typed planeswalker")
    void anyTargetIsLayerAwareForATypeReplacedPlaneswalker() {
        Permanent walker = permanents.get("planeswalker");
        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(walker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isPlaneswalker(gd, walker)).isFalse();
        assertThat(walker.getCard().hasType(CardType.PLANESWALKER)).isTrue();

        assertThat(specInterpreterAccepts(TargetCategory.ANY_TARGET, walker.getId())).isFalse();
        assertThat(matchesPermanent(TargetCategory.ANY_TARGET, walker)).isFalse();
    }

    private boolean matchesPermanent(TargetCategory category, Permanent permanent) {
        TargetPredicate predicate = TargetPredicates.forCategory(category);
        return predicate != null && sut.matchesPermanent(predicate, permanent, filterContext());
    }

    private boolean matchesPlayer(TargetCategory category, UUID playerId) {
        TargetPredicate predicate = TargetPredicates.forCategory(category);
        return predicate != null && sut.matchesPlayer(predicate, playerId, player1.getId(), gd);
    }

    private boolean matchesGraveyardCard(TargetCategory category, Card card, UUID graveyardOwnerId) {
        TargetPredicate predicate = TargetPredicates.forCategory(category);
        return predicate != null
                && sut.matchesGraveyardCard(predicate, card, graveyardOwnerId, player1.getId(), filterContext());
    }

    /**
     * Whether the real {@code TargetSpec} interpreter accepts {@code targetId} for a benign spec of
     * {@code category}. Benign so the CR 702.16b protection check (an orthogonal axis the predicate
     * deliberately does not carry) never fires.
     */
    private boolean specInterpreterAccepts(TargetCategory category, UUID targetId) {
        return targetValidationService.checkEffectTargets(
                List.of(new CategoryStubEffect(category)),
                new TargetValidationContext(gd, targetId, null, sourceCard)).isEmpty();
    }

    /**
     * The category → {@link GraveyardSearchScope} mapping the engine hand-copies today, in
     * {@code SpellCastingService:1148}, {@code AiTargetSelector:740} and
     * {@code GraveyardTargetingSupport:37}. Step 5 deletes those copies in favour of the leaf's own
     * scope; this is the reference they must agree with until then.
     */
    private boolean inScopeToday(TargetCategory category, UUID graveyardOwnerId) {
        boolean own = graveyardOwnerId.equals(player1.getId());
        return switch (category) {
            case CONTROLLERS_GRAVEYARD_CARD -> own;
            case ANY_GRAVEYARD_CARD -> true;
            case GRAVEYARD_CARD -> !own;
            default -> false;
        };
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

    /** Declares nothing but the category under test, so the interpreter is exercised in isolation. */
    private record CategoryStubEffect(TargetCategory category) implements CardEffect {
        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(TargetPredicates.forCategory(category));
        }
    }
}
