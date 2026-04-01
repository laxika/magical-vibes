package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KarnsTemporalSundering;
import com.github.laxika.magicalvibes.cards.p.Pounce;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Skulduggery;
import com.github.laxika.magicalvibes.cards.w.WizardsLightning;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@Tag("scryfall")
class AiTargetSelectorTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private AiTargetSelector targetSelector;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();

        targetSelector = new AiTargetSelector(
                harness.getGameQueryService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
    }

    // ===== findValidPermanentTargetsForManaValueX =====

    @Test
    @DisplayName("Returns creatures with mana value within affordable range")
    void returnsCreaturesWithAffordableManaValue() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 2);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Excludes creatures with mana value exceeding maxX")
    void excludesCreaturesExceedingMaxX() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 1);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Returns empty list when no creatures have affordable mana value")
    void returnsEmptyWhenNoAffordableTargets() {
        // GrizzlyBears MV=2, but maxX=1
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        // maxX=0 means nothing is affordable (MV must be >= 1)
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 0);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when no creatures on any battlefield")
    void returnsEmptyWhenNoCreatures() {
        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Includes creatures from both opponent and own battlefield")
    void includesCreaturesFromBothBattlefields() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Searches opponent battlefield before own")
    void searchesOpponentFirst() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        // Opponent's creature should be listed before own
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Excludes non-creature permanents")
    void excludesNonCreatures() {
        // Island is a land, not a creature — should be excluded by the card's target filter
        harness.addToBattlefield(human, new Island());
        harness.addToBattlefield(human, new EliteVanguard());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    // ===== "any target" spells: isValidPermanentTarget =====

    @Test
    @DisplayName("Rejects non-creature artifact for 'any target' damage spell")
    void rejectsArtifactForAnyTargetSpell() {
        // Elaborate Firecannon is a non-creature artifact — not a valid target for "any target" damage
        Permanent artifact = harness.addToBattlefieldAndReturn(human, new ElaborateFirecannon());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, artifact, aiPlayer.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Rejects land for 'any target' damage spell")
    void rejectsLandForAnyTargetSpell() {
        Permanent land = harness.addToBattlefieldAndReturn(human, new Island());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, land, aiPlayer.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Accepts creature for 'any target' damage spell")
    void acceptsCreatureForAnyTargetSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, creature, aiPlayer.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("chooseTarget picks creature, not non-creature permanent, for 'any target' spell")
    void chooseTargetSkipsNonCreatureForAnyTargetSpell() {
        // Opponent has a land and a creature — AI should only target the creature
        harness.addToBattlefield(human, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

        WizardsLightning spell = new WizardsLightning();
        UUID targetId = targetSelector.chooseTarget(gd, spell, aiPlayer.getId());

        assertThat(targetId).isEqualTo(bears.getId());
    }

    // ===== findValidGraveyardTargets: type filtering =====

    private static Card makeGraveyardCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private static Card makeBasicLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        return card;
    }

    private void setupGraveyardWithAllTypes() {
        harness.setGraveyard(human, List.of(
                makeGraveyardCard("GY Creature", CardType.CREATURE),
                makeGraveyardCard("GY Instant", CardType.INSTANT),
                makeGraveyardCard("GY Sorcery", CardType.SORCERY),
                makeGraveyardCard("GY Artifact", CardType.ARTIFACT),
                makeGraveyardCard("GY Enchantment", CardType.ENCHANTMENT),
                makeBasicLand("GY Basic Land")
        ));
    }

    static Stream<Arguments> graveyardEffectFilterCases() {
        return Stream.of(
                Arguments.of(
                        "PutCreatureFromOpponentGraveyard filters to creatures only",
                        new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect(),
                        Set.of("GY Creature")
                ),
                Arguments.of(
                        "CastTargetInstantOrSorceryFromGraveyard filters to instants and sorceries",
                        new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.OPPONENT_GRAVEYARD, false),
                        Set.of("GY Instant", "GY Sorcery")
                ),
                Arguments.of(
                        "ExileTargetCardFromGraveyard(CREATURE) filters to creatures only",
                        new ExileTargetCardFromGraveyardEffect(CardType.CREATURE),
                        Set.of("GY Creature")
                ),
                Arguments.of(
                        "ExileTargetCardFromGraveyard(null) allows all card types",
                        new ExileTargetCardFromGraveyardEffect(null),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment", "GY Basic Land")
                ),
                Arguments.of(
                        "GrantFlashbackToTargetGraveyardCard filters to matching card types",
                        new GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                        Set.of("GY Instant", "GY Sorcery")
                ),
                Arguments.of(
                        "ExileTargetCardFromGraveyardAndImprint(ARTIFACT) filters to artifacts only",
                        new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(new CardTypePredicate(CardType.ARTIFACT)),
                        Set.of("GY Artifact")
                ),
                Arguments.of(
                        "PutCardFromOpponentGraveyard filters to artifacts and creatures",
                        new PutCardFromOpponentGraveyardOntoBattlefieldEffect(),
                        Set.of("GY Creature", "GY Artifact")
                ),
                Arguments.of(
                        "ExileTargetGraveyardCardAndSameName excludes basic lands",
                        new ExileTargetGraveyardCardAndSameNameFromZonesEffect(),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment")
                ),
                Arguments.of(
                        "ExileGraveyardCardWithConditionalBonus allows all card types",
                        new ExileGraveyardCardWithConditionalBonusEffect(3, 1, 1),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment", "GY Basic Land")
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("graveyardEffectFilterCases")
    @DisplayName("findValidGraveyardTargets filters by effect type")
    void findValidGraveyardTargets_filtersByEffectType(String description, CardEffect effect, Set<String> expectedNames) {
        setupGraveyardWithAllTypes();

        Card spellCard = new Card();
        spellCard.setName("Test Spell");
        spellCard.setType(CardType.SORCERY);
        spellCard.addEffect(EffectSlot.SPELL, effect);

        List<Card> results = targetSelector.findValidGraveyardTargets(gd, spellCard, aiPlayer.getId());

        Set<String> resultNames = results.stream().map(Card::getName).collect(java.util.stream.Collectors.toSet());
        assertThat(resultNames).isEqualTo(expectedNames);
    }

    @Test
    @DisplayName("findValidGraveyardTargets returns empty when no cards match filter")
    void findValidGraveyardTargets_emptyWhenNoMatch() {
        // Only non-creature cards in graveyard
        harness.setGraveyard(human, List.of(
                makeGraveyardCard("GY Instant", CardType.INSTANT),
                makeBasicLand("GY Basic Land")
        ));

        Card spellCard = new Card();
        spellCard.setName("Test Spell");
        spellCard.setType(CardType.SORCERY);
        spellCard.addEffect(EffectSlot.SPELL, new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect());

        List<Card> results = targetSelector.findValidGraveyardTargets(gd, spellCard, aiPlayer.getId());

        assertThat(results).isEmpty();
    }

    // =====================================================================
    // Mockito-based tests for buildDamageAssignments, computeBaseAllowedTargets,
    // and EffectResolution.needsDamageDistribution
    // =====================================================================

    @Nested
    @DisplayName("buildDamageAssignments (unit)")
    class BuildDamageAssignmentsUnit {

        private GameQueryService mockGqs;
        private TargetValidationService mockTvs;
        private AiTargetSelector unitSelector;
        private GameData unitGd;
        private UUID aiId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            mockGqs = mock(GameQueryService.class);
            mockTvs = mock(TargetValidationService.class);
            unitSelector = new AiTargetSelector(mockGqs, mockTvs, null);

            aiId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            unitGd = new GameData(UUID.randomUUID(), "test", aiId, "AI");
            unitGd.orderedPlayerIds.add(aiId);
            unitGd.orderedPlayerIds.add(opponentId);
            unitGd.playerIds.add(aiId);
            unitGd.playerIds.add(opponentId);
            unitGd.playerBattlefields.put(aiId, Collections.synchronizedList(new ArrayList<>()));
            unitGd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

            // Default: all effect-level target validations pass
            lenient().when(mockTvs.checkEffectTargets(any(), any())).thenReturn(Optional.empty());
        }

        private Permanent addCreature(UUID owner, String name, int toughness) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            Permanent perm = new Permanent(card);
            unitGd.playerBattlefields.get(owner).add(perm);
            lenient().when(mockGqs.isCreature(unitGd, perm)).thenReturn(true);
            lenient().when(mockGqs.getEffectiveToughness(unitGd, perm)).thenReturn(toughness);
            return perm;
        }

        @Test
        @DisplayName("Returns null for spell with no divided damage effect")
        void returnsNullForNonDividedDamageSpell() {
            Card bolt = new Card();
            bolt.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            assertThat(unitSelector.buildDamageAssignments(unitGd, bolt, aiId)).isNull();
        }

        @Test
        @DisplayName("Distributes creature-only divided damage to opponent's creatures")
        void distributesCreatureOnlyDividedDamage() {
            addCreature(opponentId, "Bear", 2);

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.target(1, 3).addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(3);
        }

        @Test
        @DisplayName("Returns null for creature-only divided damage when no valid creatures")
        void returnsNullForCreatureOnlyWithNoTargets() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.target(1, 3).addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));

            assertThat(unitSelector.buildDamageAssignments(unitGd, spell, aiId)).isNull();
        }

        @Test
        @DisplayName("Handles DealDividedDamageAmongAnyTargetsEffect inside KickerReplacementEffect")
        void handlesAnyTargetDividedDamageFromKicker() {
            addCreature(opponentId, "Bear", 2);

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                    new DealDamageToTargetCreatureEffect(5),
                    new DealDividedDamageAmongAnyTargetsEffect(10)));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(10);
        }

        @Test
        @DisplayName("Any-target divided damage sends remaining damage to opponent player")
        void anyTargetDumpsRemainingOnOpponent() {
            addCreature(opponentId, "Bear", 2); // needs 2 lethal, remaining 8 → opponent

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDividedDamageAmongAnyTargetsEffect(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).containsEntry(opponentId, 8);
        }

        @Test
        @DisplayName("Any-target divided damage targets opponent when no creatures exist")
        void anyTargetTargetsOpponentWhenNoCreatures() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDividedDamageAmongAnyTargetsEffect(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result).containsEntry(opponentId, 10);
        }

        @Test
        @DisplayName("Any-target divided damage kills multiple creatures then dumps remainder on opponent")
        void anyTargetKillsMultipleCreaturesThenDumpsOnOpponent() {
            addCreature(opponentId, "Goblin", 1);  // 1 lethal
            addCreature(opponentId, "Bear", 2);    // 2 lethal → remaining 7 on opponent

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDividedDamageAmongAnyTargetsEffect(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(10);
            assertThat(result).containsEntry(opponentId, 7);
        }
    }

    // ===== chooseTarget: player hexproof / shroud =====

    @Nested
    @DisplayName("chooseTarget player hexproof / shroud (unit)")
    class ChooseTargetPlayerHexproofShroudUnit {

        private GameQueryService mockGqs;
        private TargetValidationService mockTvs;
        private AiTargetSelector unitSelector;
        private GameData unitGd;
        private UUID aiId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            mockGqs = mock(GameQueryService.class);
            mockTvs = mock(TargetValidationService.class);
            unitSelector = new AiTargetSelector(mockGqs, mockTvs, null);

            aiId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            unitGd = new GameData(UUID.randomUUID(), "test", aiId, "AI");
            unitGd.orderedPlayerIds.add(aiId);
            unitGd.orderedPlayerIds.add(opponentId);
            unitGd.playerIds.add(aiId);
            unitGd.playerIds.add(opponentId);
            unitGd.playerBattlefields.put(aiId, Collections.synchronizedList(new ArrayList<>()));
            unitGd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

            lenient().when(mockTvs.checkEffectTargets(any(), any())).thenReturn(Optional.empty());
        }

        private Card makePlayerOnlySpell() {
            Card spell = new Card();
            spell.setName("Test Player Spell");
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));
            return spell;
        }

        @Test
        @DisplayName("Returns null when opponent has hexproof")
        void returnsNullWhenOpponentHasHexproof() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(false);
            lenient().when(mockGqs.playerHasHexproof(unitGd, opponentId)).thenReturn(true);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Returns null when opponent has shroud")
        void returnsNullWhenOpponentHasShroud() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(true);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Returns opponent when opponent has no hexproof or shroud")
        void returnsOpponentWhenNoProtection() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(false);
            lenient().when(mockGqs.playerHasHexproof(unitGd, opponentId)).thenReturn(false);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isEqualTo(opponentId);
        }
    }

    // ===== computeBaseAllowedTargets =====

    @Nested
    @DisplayName("computeBaseAllowedTargets (unit)")
    class ComputeBaseAllowedTargetsUnit {

        private AiTargetSelector unitSelector;

        @BeforeEach
        void setUp() {
            unitSelector = new AiTargetSelector(
                    mock(GameQueryService.class), mock(TargetValidationService.class), null);
        }

        @Test
        @DisplayName("KickerReplacementEffect uses only base effect targeting")
        void kickerReplacementUsesBaseOnly() {
            // Base: creature only (canTargetPermanent=true, canTargetPlayer=false)
            // Kicked: any targets (canTargetPermanent=true, canTargetPlayer=true)
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                    new DealDamageToTargetCreatureEffect(5),
                    new DealDividedDamageAmongAnyTargetsEffect(10)));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).doesNotContain(TargetType.PLAYER);
        }

        @Test
        @DisplayName("Non-wrapped any-target effect includes both PERMANENT and PLAYER")
        void nonWrappedAnyTargetIncludesBoth() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).contains(TargetType.PLAYER);
        }

        @Test
        @DisplayName("Creature-only effect includes only PERMANENT")
        void creatureOnlyIncludesPermanent() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).doesNotContain(TargetType.PLAYER);
        }
    }

    // ===== EffectResolution.needsDamageDistribution =====

    @Nested
    @DisplayName("needsDamageDistribution (unit)")
    class NeedsDamageDistributionUnit {

        @Test
        @DisplayName("Returns true for DealDividedDamageAmongTargetCreaturesEffect")
        void trueForCreatureOnlyDividedDamage() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isTrue();
        }

        @Test
        @DisplayName("Returns true for direct DealDividedDamageAmongAnyTargetsEffect")
        void trueForDirectAnyTargetDividedDamage() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new DealDividedDamageAmongAnyTargetsEffect(5));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isTrue();
        }

        @Test
        @DisplayName("Returns false for regular damage spell")
        void falseForRegularDamageSpell() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isFalse();
        }

        @Test
        @DisplayName("Returns false when effect is wrapped in KickerReplacementEffect")
        void falseForWrappedKickerEffect() {
            // The DealDividedDamageAmongAnyTargetsEffect is inside a KickerReplacementEffect,
            // so needsDamageDistribution should return false (the wrapper type doesn't match)
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                    new DealDamageToTargetCreatureEffect(5),
                    new DealDividedDamageAmongAnyTargetsEffect(10)));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isFalse();
        }
    }

    // ===== chooseMultiTargets =====

    @Nested
    @DisplayName("chooseMultiTargets")
    class ChooseMultiTargetsTests {

        @Test
        @DisplayName("Karn's Temporal Sundering: picks self for extra turn + opponent's nonland permanent")
        void karnsTemporalSunderingPicksSelfAndOpponentPermanent() {
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            // First target: AI player (beneficial extra turn)
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
            // Second target: opponent's creature
            Permanent oppPerm = gd.playerBattlefields.get(human.getId()).getFirst();
            assertThat(targets.get(1)).isEqualTo(oppPerm.getId());
        }

        @Test
        @DisplayName("Karn's Temporal Sundering: returns player only when no nonland permanents exist")
        void karnsTemporalSunderingOptionalSecondTarget() {
            // No nonland permanents on either battlefield
            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            // Second target is optional (min=0), so should still succeed with just the player
            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(1);
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
        }

        @Test
        @DisplayName("Karn's Temporal Sundering: skips lands for second target")
        void karnsTemporalSunderingSkipsLands() {
            // Only a land on the battlefield — not a valid nonland permanent target
            harness.addToBattlefield(human, new Island());

            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(1); // Only player, no valid nonland permanent
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
        }

        @Test
        @DisplayName("Pounce: picks own creature and opponent's creature for fight")
        void pouncePicksOwnAndOpponentCreature() {
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            // First target: own creature (ControlledPermanentPredicateTargetFilter)
            Permanent ownPerm = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                    .findFirst().orElseThrow();
            assertThat(targets.get(0)).isEqualTo(ownPerm.getId());
            // Second target: opponent's creature
            Permanent oppPerm = gd.playerBattlefields.get(human.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(targets.get(1)).isEqualTo(oppPerm.getId());
        }

        @Test
        @DisplayName("Pounce: returns null when AI has no creature to fight with")
        void pounceReturnsNullWithoutOwnCreature() {
            // Only opponent has a creature — first target (creature you control) can't be satisfied
            harness.addToBattlefield(human, new GrizzlyBears());

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNull();
        }

        @Test
        @DisplayName("Pounce: returns null when opponent has no creature to fight")
        void pounceReturnsNullWithoutOpponentCreature() {
            // Only AI has a creature — second target (creature you don't control) can't be satisfied
            harness.addToBattlefield(aiPlayer, new EliteVanguard());

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNull();
        }

        @Test
        @DisplayName("Skulduggery: picks own creature for buff and opponent's creature for debuff")
        void skulduggeryPicksBothTargets() {
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Skulduggery card = new Skulduggery();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
        }

        @Test
        @DisplayName("Does not reuse the same target across groups")
        void doesNotReuseSameTarget() {
            // Only one creature on each side — targets must be distinct
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            assertThat(targets.get(0)).isNotEqualTo(targets.get(1));
        }
    }

    // ===== chooseSpellTarget =====

    @Nested
    @DisplayName("chooseSpellTarget")
    class ChooseSpellTarget {

        @Test
        @DisplayName("Selects opponent creature spell on the stack for Cancel")
        void selectsOpponentCreatureSpellForCancel() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears();
            StackEntry creatureSpell = new StackEntry(bears, human.getId());
            gd.stack.add(creatureSpell);

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Returns null when stack is empty")
        void returnsNullForEmptyStack() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Does not target AI's own spells")
        void doesNotTargetOwnSpells() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears();
            StackEntry ownSpell = new StackEntry(bears, aiPlayer.getId());
            gd.stack.add(ownSpell);

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Picks highest-value spell when multiple spells on stack")
        void picksHighestValueSpell() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears(); // MV=2
            SerraAngel angel = new SerraAngel(); // MV=5
            gd.stack.add(new StackEntry(bears, human.getId()));
            gd.stack.add(new StackEntry(angel, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isEqualTo(angel.getId());
        }

        @Test
        @DisplayName("Essence Scatter only targets creature spells")
        void essenceScatterOnlyTargetsCreatureSpells() {
            com.github.laxika.magicalvibes.cards.e.EssenceScatter scatter =
                    new com.github.laxika.magicalvibes.cards.e.EssenceScatter();

            // Sorcery spell on the stack
            WizardsLightning bolt = new WizardsLightning();
            StackEntry sorcerySpell = new StackEntry(
                    com.github.laxika.magicalvibes.model.StackEntryType.INSTANT_SPELL,
                    bolt, human.getId(), "Wizard's Lightning",
                    bolt.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL), 0);
            gd.stack.add(sorcerySpell);

            UUID target = targetSelector.chooseSpellTarget(gd, scatter, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Essence Scatter targets creature spell when present")
        void essenceScatterTargetsCreatureSpell() {
            com.github.laxika.magicalvibes.cards.e.EssenceScatter scatter =
                    new com.github.laxika.magicalvibes.cards.e.EssenceScatter();

            GrizzlyBears bears = new GrizzlyBears();
            gd.stack.add(new StackEntry(bears, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, scatter, aiPlayer.getId());

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Negate does not target creature spells")
        void negateDoesNotTargetCreatureSpells() {
            com.github.laxika.magicalvibes.cards.n.Negate negate =
                    new com.github.laxika.magicalvibes.cards.n.Negate();

            GrizzlyBears bears = new GrizzlyBears();
            gd.stack.add(new StackEntry(bears, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, negate, aiPlayer.getId());

            assertThat(target).isNull();
        }
    }
}
