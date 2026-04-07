package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameBroadcastServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private PermanentViewFactory permanentViewFactory;
    @Mock private StackEntryViewFactory stackEntryViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private ValidTargetService validTargetService;

    private GameBroadcastService svc;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    private static final GameQueryService.StaticBonus NO_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(),
            false, false, false, Set.of(), false, 0, 0, false);

    @BeforeEach
    void setUp() {
        svc = new GameBroadcastService(sessionManager, cardViewFactory, permanentViewFactory,
                stackEntryViewFactory, gameQueryService, validTargetService);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    @Nested
    @DisplayName("isSpellCastingAllowed — legendary sorcery restriction")
    class LegendarySorceryRestriction {

        @Test
        @DisplayName("Rejects legendary sorcery when player controls no legendary creature or planeswalker")
        void rejectsLegendarySorceryWithoutLegendaryPermanent() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Urza's Ruinous Blast");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{4}{W}");

            // Only a non-legendary creature on battlefield
            Card bears = new Card();
            bears.setName("Grizzly Bears");
            bears.setType(CardType.CREATURE);
            Permanent bearsPerm = new Permanent(bears);
            gd.playerBattlefields.get(player1Id).add(bearsPerm);

            when(gameQueryService.computeStaticBonus(any(), any())).thenReturn(NO_BONUS);

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isFalse();
        }

        @Test
        @DisplayName("Allows legendary sorcery when player controls a legendary creature")
        void allowsLegendarySorceryWithLegendaryCreature() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Urza's Ruinous Blast");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{4}{W}");

            Card legendaryCreature = new Card();
            legendaryCreature.setName("Arvad the Cursed");
            legendaryCreature.setType(CardType.CREATURE);
            legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            Permanent legendaryPerm = new Permanent(legendaryCreature);
            gd.playerBattlefields.get(player1Id).add(legendaryPerm);

            when(gameQueryService.computeStaticBonus(any(), any())).thenReturn(NO_BONUS);
            when(gameQueryService.isCreature(any(), any())).thenReturn(true);

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isTrue();
        }

        @Test
        @DisplayName("Allows non-legendary sorcery regardless of battlefield state")
        void allowsNonLegendarySorcery() {
            Card normalSorcery = new Card();
            normalSorcery.setName("Divination");
            normalSorcery.setType(CardType.SORCERY);
            normalSorcery.setManaCost("{2}{U}");

            // Empty battlefield — no legendary permanents
            assertThat(svc.isSpellCastingAllowed(gd, player1Id, normalSorcery)).isTrue();
        }

        @Test
        @DisplayName("Allows legendary non-sorcery (e.g. legendary creature) regardless of battlefield state")
        void allowsLegendaryNonSorcery() {
            Card legendaryCreature = new Card();
            legendaryCreature.setName("Arvad the Cursed");
            legendaryCreature.setType(CardType.CREATURE);
            legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendaryCreature.setManaCost("{3}{W}{B}");

            // Empty battlefield
            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendaryCreature)).isTrue();
        }

        @Test
        @DisplayName("Rejects legendary sorcery with empty battlefield")
        void rejectsLegendarySorceryWithEmptyBattlefield() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Kamahl's Druidic Vow");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{X}{G}{G}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isFalse();
        }
    }

    @Nested
    @DisplayName("buildCostModifierSnapshot — single-pass battlefield collection")
    class CostModifierSnapshotTests {

        @Test
        @DisplayName("Empty battlefields produce zero-cost snapshot")
        void emptyBattlefieldsProduceZeroCostSnapshot() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.opponentIncreases()).isEmpty();
            assertThat(snapshot.spellCastTax()).isZero();
            assertThat(snapshot.predicateIncreases()).isEmpty();
            assertThat(snapshot.cardTypeReductions()).isEmpty();
            assertThat(snapshot.subtypeReductions()).isEmpty();
            assertThat(snapshot.selfMatchReductions()).isEmpty();
            assertThat(snapshot.opponentMatchReductions()).isEmpty();
        }

        @Test
        @DisplayName("Collects opponent cost increase from opponent's battlefield")
        void collectsOpponentCostIncrease() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Thalia");
            taxCard.setType(CardType.CREATURE);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.opponentIncreases()).hasSize(1);
            assertThat(snapshot.opponentIncreases().getFirst().amount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not collect own permanents as opponent cost increases")
        void ownPermanentsNotCollectedAsOpponentIncreases() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Thalia");
            taxCard.setType(CardType.CREATURE);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT), 1));
            // Put it on player1's battlefield — should NOT be collected as opponent increase
            gd.playerBattlefields.get(player1Id).add(new Permanent(taxCard));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.opponentIncreases()).isEmpty();
        }

        @Test
        @DisplayName("Collects spell-cast tax from any player's battlefield")
        void collectsSpellCastTax() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Sphere of Resistance");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseEachPlayerCastCostPerSpellThisTurnEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(taxCard));
            Card dummySpell = new Card();
            dummySpell.setName("Dummy");
            dummySpell.setType(CardType.INSTANT);
            gd.recordSpellCast(player1Id, dummySpell);
            gd.recordSpellCast(player1Id, dummySpell);

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            // 1 per spell × 2 spells cast = 2
            assertThat(snapshot.spellCastTax()).isEqualTo(2);
        }

        @Test
        @DisplayName("Spell-cast tax is zero when no spells cast")
        void spellCastTaxZeroWhenNoSpellsCast() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Sphere of Resistance");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseEachPlayerCastCostPerSpellThisTurnEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.spellCastTax()).isZero();
        }

        @Test
        @DisplayName("Collects own card-type cost reduction")
        void collectsOwnCardTypeReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.cardTypeReductions()).hasSize(1);
            assertThat(snapshot.cardTypeReductions().getFirst().amount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Does not collect opponent's card-type reduction as own")
        void opponentCardTypeReductionNotCollectedAsOwn() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.cardTypeReductions()).isEmpty();
        }

        @Test
        @DisplayName("Collects SELF-scoped match reduction from own battlefield")
        void collectsSelfScopedMatchReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Jhoira's Familiar");
            reducer.setType(CardType.CREATURE);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardTypePredicate(CardType.ARTIFACT), 1, CostModificationScope.SELF));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.selfMatchReductions()).hasSize(1);
            assertThat(snapshot.opponentMatchReductions()).isEmpty();
        }

        @Test
        @DisplayName("Collects OPPONENT-scoped match reduction from opponent's battlefield")
        void collectsOpponentScopedMatchReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Opponent Reducer");
            reducer.setType(CardType.CREATURE);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.OPPONENT));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.opponentMatchReductions()).hasSize(1);
            assertThat(snapshot.selfMatchReductions()).isEmpty();
        }

        @Test
        @DisplayName("Collects subtype reduction from own battlefield")
        void collectsSubtypeReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Goblin Warchief");
            reducer.setType(CardType.CREATURE);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForSubtypeEffect(Set.of(CardSubtype.GOBLIN), 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.subtypeReductions()).hasSize(1);
        }

        @Test
        @DisplayName("Collects predicate-based spell cost increase from any battlefield")
        void collectsPredicateSpellCostIncrease() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Thorn of Amethyst");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseSpellCostEffect(new CardTypePredicate(CardType.INSTANT), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(snapshot.predicateIncreases()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getCastCostModifier with snapshot — evaluates per card")
    class GetCastCostModifierWithSnapshotTests {

        @Test
        @DisplayName("Applies opponent cost increase for matching card type")
        void appliesOpponentCostIncreaseForMatchingType() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card taxCard = new Card();
            taxCard.setName("Thalia");
            taxCard.setType(CardType.CREATURE);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            // Instant should be taxed
            assertThat(svc.getCastCostModifier(gd, player1Id, instant, snapshot)).isEqualTo(1);
            // Creature should not be taxed
            assertThat(svc.getCastCostModifier(gd, player1Id, creature, snapshot)).isZero();
        }

        @Test
        @DisplayName("Applies own card-type cost reduction for matching type")
        void appliesOwnCardTypeReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            Card enchantment = new Card();
            enchantment.setName("Pacifism");
            enchantment.setType(CardType.ENCHANTMENT);
            enchantment.setManaCost("{1}{W}");

            // Creature gets -2 reduction
            assertThat(svc.getCastCostModifier(gd, player1Id, creature, snapshot)).isEqualTo(-2);
            // Enchantment is unaffected
            assertThat(svc.getCastCostModifier(gd, player1Id, enchantment, snapshot)).isZero();
        }

        @Test
        @DisplayName("Combines increase and reduction correctly")
        void combinesIncreaseAndReduction() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            // Opponent has Thalia: +1 to noncreatures (instants/sorceries)
            Card thalia = new Card();
            thalia.setName("Thalia");
            thalia.setType(CardType.CREATURE);
            thalia.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(thalia));

            // Player has a SELF-scoped reducer for instants: -1
            Card familiar = new Card();
            familiar.setName("Reducer");
            familiar.setType(CardType.ARTIFACT);
            familiar.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardTypePredicate(CardType.INSTANT), 1, CostModificationScope.SELF));
            gd.playerBattlefields.get(player1Id).add(new Permanent(familiar));

            when(gameQueryService.matchesCardPredicate(any(), any(), any())).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                CardTypePredicate pred = invocation.getArgument(1);
                return card.hasType(pred.cardType());
            });

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");

            // +1 from Thalia, -1 from reducer = net 0
            assertThat(svc.getCastCostModifier(gd, player1Id, bolt, snapshot)).isZero();
        }

        @Test
        @DisplayName("Subtype reduction applies when card has matching subtype")
        void subtypeReductionAppliesForMatchingSubtype() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);

            Card warchief = new Card();
            warchief.setName("Goblin Warchief");
            warchief.setType(CardType.CREATURE);
            warchief.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForSubtypeEffect(Set.of(CardSubtype.GOBLIN), 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(warchief));

            when(gameQueryService.cardHasSubtype(any(), eq(CardSubtype.GOBLIN), any(), any()))
                    .thenAnswer(inv -> {
                        Card c = inv.getArgument(0);
                        return c.getSubtypes().contains(CardSubtype.GOBLIN);
                    });

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            Card goblin = new Card();
            goblin.setName("Goblin Piker");
            goblin.setType(CardType.CREATURE);
            goblin.setManaCost("{1}{R}");
            goblin.setSubtypes(List.of(CardSubtype.GOBLIN));

            Card elf = new Card();
            elf.setName("Llanowar Elves");
            elf.setType(CardType.CREATURE);
            elf.setManaCost("{G}");
            elf.setSubtypes(List.of(CardSubtype.ELF));

            assertThat(svc.getCastCostModifier(gd, player1Id, goblin, snapshot)).isEqualTo(-1);
            assertThat(svc.getCastCostModifier(gd, player1Id, elf, snapshot)).isZero();
        }
    }

    @Nested
    @DisplayName("getPlayableCardIndices — uses snapshot for cost modifiers")
    class GetPlayableCardIndicesTests {

        @Test
        @DisplayName("Creature with cost reduction becomes playable")
        void creatureWithCostReductionIsPlayable() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            // Heartless Summoning: creatures cost {2} less
            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            // Player has {G} and a {2}{G} creature — with -2 reduction, effective cost is {G} only
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);

            Card creature = new Card();
            creature.setName("Centaur Courser");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{2}{G}");
            gd.playerHands.get(player1Id).add(creature);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).contains(0);
        }

        @Test
        @DisplayName("Spell taxed by opponent permanent becomes unplayable without enough mana")
        void spellTaxedByOpponentBecomesUnplayable() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            // Opponent has Thalia: instants/sorceries cost {1} more
            Card thalia = new Card();
            thalia.setName("Thalia");
            thalia.setType(CardType.CREATURE);
            thalia.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(thalia));

            // Player has exactly {R} — Lightning Bolt costs {R} + {1} tax = can't afford
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.RED);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");
            gd.playerHands.get(player1Id).add(bolt);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).doesNotContain(0);
        }

        @Test
        @DisplayName("Land is playable during main phase regardless of cost modifiers")
        void landPlayableDuringMainPhase() {
            when(gameQueryService.getOpponentId(any(), eq(player1Id))).thenReturn(player2Id);
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            Card land = new Card();
            land.setName("Forest");
            land.setType(CardType.LAND);
            gd.playerHands.get(player1Id).add(land);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).contains(0);
        }
    }
}
