package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
class CastingCostServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;

    private CastingCostService svc;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        CostModificationSupport support = new CostModificationSupport(gameQueryService, predicateEvaluationService);
        svc = new CastingCostService(
                CostModificationTestRegistry.build(gameQueryService, predicateEvaluationService, support),
                support, gameQueryService, predicateEvaluationService);

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
    @DisplayName("getCastCostModifier — handler-dispatched cost modifiers")
    class GetCastCostModifierTests {

        @Test
        @DisplayName("Applies opponent cost increase for matching card type")
        void appliesOpponentCostIncreaseForMatchingType() {
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
        @DisplayName("Own permanent with opponent-tax effect does not tax own spells")
        void ownPermanentsDoNotTaxOwnSpells() {
            Card taxCard = new Card();
            taxCard.setName("Thalia");
            taxCard.setType(CardType.CREATURE);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT), 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(taxCard));

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            assertThat(svc.getCastCostModifier(gd, player1Id, instant)).isZero();
        }

        @Test
        @DisplayName("Spell-cast tax multiplies by spells cast this turn")
        void spellCastTaxMultipliesBySpellsCast() {
            Card taxCard = new Card();
            taxCard.setName("Damping Sphere");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseEachPlayerCastCostPerSpellThisTurnEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(taxCard));
            Card dummySpell = new Card();
            dummySpell.setName("Dummy");
            dummySpell.setType(CardType.INSTANT);
            gd.recordSpellCast(player1Id, dummySpell);
            gd.recordSpellCast(player1Id, dummySpell);

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            // 1 per spell × 2 spells cast = 2
            assertThat(svc.getCastCostModifier(gd, player1Id, instant)).isEqualTo(2);
        }

        @Test
        @DisplayName("Spell-cast tax is zero when no spells cast")
        void spellCastTaxZeroWhenNoSpellsCast() {
            Card taxCard = new Card();
            taxCard.setName("Damping Sphere");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseEachPlayerCastCostPerSpellThisTurnEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            assertThat(svc.getCastCostModifier(gd, player1Id, instant)).isZero();
        }

        @Test
        @DisplayName("Applies own card-type cost reduction for matching type")
        void appliesOwnCardTypeReduction() {
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
        @DisplayName("Opponent's own-cast reduction does not reduce this player's spells")
        void opponentCardTypeReductionDoesNotApply() {
            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            assertThat(svc.getCastCostModifier(gd, player1Id, creature)).isZero();
        }

        @Test
        @DisplayName("OPPONENT-scoped match reduction on opponent's battlefield reduces this player's spells")
        void opponentScopedMatchReductionApplies() {
            Card reducer = new Card();
            reducer.setName("Opponent Reducer");
            reducer.setType(CardType.CREATURE);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.OPPONENT));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));

            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any())).thenAnswer(inv -> {
                Card card = inv.getArgument(0);
                CardTypePredicate pred = inv.getArgument(1);
                return card.hasType(pred.cardType());
            });

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            assertThat(svc.getCastCostModifier(gd, player1Id, creature)).isEqualTo(-1);
        }

        @Test
        @DisplayName("SELF-scoped match reduction on own battlefield does not reduce opponent's spells")
        void selfScopedMatchReductionDoesNotApplyToOpponent() {
            Card reducer = new Card();
            reducer.setName("Jhoira's Familiar");
            reducer.setType(CardType.CREATURE);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardTypePredicate(CardType.ARTIFACT), 1, CostModificationScope.SELF));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            Card artifact = new Card();
            artifact.setName("Mind Stone");
            artifact.setType(CardType.ARTIFACT);
            artifact.setManaCost("{2}");

            // player2 casting: player1's SELF-scoped reducer must not apply
            assertThat(svc.getCastCostModifier(gd, player2Id, artifact)).isZero();
        }

        @Test
        @DisplayName("Predicate-based spell cost increase applies from any battlefield")
        void predicateSpellCostIncreaseApplies() {
            Card taxCard = new Card();
            taxCard.setName("Thorn of Amethyst");
            taxCard.setType(CardType.ARTIFACT);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseSpellCostEffect(new CardTypePredicate(CardType.INSTANT), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any())).thenAnswer(inv -> {
                Card card = inv.getArgument(0);
                CardTypePredicate pred = inv.getArgument(1);
                return card.hasType(pred.cardType());
            });

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            assertThat(svc.getCastCostModifier(gd, player1Id, instant)).isEqualTo(1);
        }

        @Test
        @DisplayName("Applies own controlled-permanent predicate reduction")
        void appliesOwnControlledPermanentPredicateReduction() {
            Card wizard = new Card();
            wizard.setName("Aether Adept");
            wizard.setType(CardType.CREATURE);
            Permanent wizardPermanent = new Permanent(wizard);
            gd.playerBattlefields.get(player1Id).add(wizardPermanent);

            Card retort = new Card();
            retort.setName("Wizard's Retort");
            retort.setType(CardType.INSTANT);
            retort.setManaCost("{1}{U}{U}");
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.WIZARD);
            retort.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfControlsPermanentEffect(predicate, 1));

            when(predicateEvaluationService.matchesPermanentPredicate(gd, wizardPermanent, predicate)).thenReturn(true);

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);

            assertThat(svc.getCastCostModifier(gd, player1Id, retort, snapshot)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Per-creature-on-battlefield reduction counts creatures of all players")
        void perCreatureReductionCountsAllBattlefields() {
            Card ownCreature = new Card();
            ownCreature.setName("Grizzly Bears");
            ownCreature.setType(CardType.CREATURE);
            gd.playerBattlefields.get(player1Id).add(new Permanent(ownCreature));
            Card opponentCreature = new Card();
            opponentCreature.setName("Hill Giant");
            opponentCreature.setType(CardType.CREATURE);
            gd.playerBattlefields.get(player2Id).add(new Permanent(opponentCreature));

            when(gameQueryService.isCreature(any(), any())).thenReturn(true);

            Card blasphemousAct = new Card();
            blasphemousAct.setName("Blasphemous Act");
            blasphemousAct.setType(CardType.SORCERY);
            blasphemousAct.setManaCost("{8}{R}");
            blasphemousAct.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostPerCreatureOnBattlefieldEffect(1));

            assertThat(svc.getCastCostModifier(gd, player1Id, blasphemousAct)).isEqualTo(-2);
        }

        @Test
        @DisplayName("Combines increase and reduction correctly")
        void combinesIncreaseAndReduction() {
            // Opponent has Thalia: +1 to instants/sorceries
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

            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any())).thenAnswer(invocation -> {
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
        @DisplayName("Subtype match reduction applies when card has matching subtype")
        void subtypeMatchReductionAppliesForMatchingSubtype() {
            Card warchief = new Card();
            warchief.setName("Goblin Warchief");
            warchief.setType(CardType.CREATURE);
            warchief.addEffect(EffectSlot.STATIC,
                    new ReduceCastCostForMatchingSpellsEffect(
                            new CardSubtypePredicate(CardSubtype.GOBLIN), 1, CostModificationScope.SELF));
            gd.playerBattlefields.get(player1Id).add(new Permanent(warchief));

            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any()))
                    .thenAnswer(inv -> {
                        Card c = inv.getArgument(0);
                        CardSubtypePredicate pred = inv.getArgument(1);
                        return c.getSubtypes().contains(pred.subtype());
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

        @Test
        @DisplayName("One-off and snapshot-based computation agree")
        void oneOffAndSnapshotAgree() {
            Card taxCard = new Card();
            taxCard.setName("Thalia");
            taxCard.setType(CardType.CREATURE);
            taxCard.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(taxCard));

            Card instant = new Card();
            instant.setName("Lightning Bolt");
            instant.setType(CardType.INSTANT);
            instant.setManaCost("{R}");

            var snapshot = svc.buildCostModifierSnapshot(gd, player1Id);
            assertThat(svc.getCastCostModifier(gd, player1Id, instant))
                    .isEqualTo(svc.getCastCostModifier(gd, player1Id, instant, snapshot));
        }
    }

    @Nested
    @DisplayName("computeTargetBasedCostReduction")
    class TargetBasedCostReduction {

        @Test
        @DisplayName("Controlled-permanent reduction applies when first target is a matching controlled permanent")
        void controlledPermanentReductionApplies() {
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
            Card stomp = new Card();
            stomp.setName("Savage Stomp");
            stomp.setType(CardType.SORCERY);
            stomp.setManaCost("{2}{G}");
            stomp.addEffect(EffectSlot.STATIC,
                    new com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledPermanentEffect(predicate, 2));

            Card dinoCard = new Card();
            dinoCard.setName("Dinosaur");
            dinoCard.setType(CardType.CREATURE);
            Permanent dinosaur = new Permanent(dinoCard);
            gd.playerBattlefields.get(player1Id).add(dinosaur);

            when(gameQueryService.findPermanentById(gd, dinosaur.getId())).thenReturn(dinosaur);
            when(gameQueryService.findPermanentController(gd, dinosaur.getId())).thenReturn(player1Id);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, dinosaur, predicate)).thenReturn(true);

            assertThat(svc.computeTargetBasedCostReduction(gd, player1Id, stomp, List.of(dinosaur.getId())))
                    .isEqualTo(2);
        }

        @Test
        @DisplayName("Controlled-permanent reduction does not apply when first target is an opponent's permanent")
        void controlledPermanentReductionDoesNotApplyToOpponentTarget() {
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
            Card stomp = new Card();
            stomp.setName("Savage Stomp");
            stomp.setType(CardType.SORCERY);
            stomp.setManaCost("{2}{G}");
            stomp.addEffect(EffectSlot.STATIC,
                    new com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledPermanentEffect(predicate, 2));

            Card bearCard = new Card();
            bearCard.setName("Bear");
            bearCard.setType(CardType.CREATURE);
            Permanent bear = new Permanent(bearCard);
            gd.playerBattlefields.get(player2Id).add(bear);

            when(gameQueryService.findPermanentById(gd, bear.getId())).thenReturn(bear);
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player2Id);

            assertThat(svc.computeTargetBasedCostReduction(gd, player1Id, stomp, List.of(bear.getId())))
                    .isZero();
        }

        @Test
        @DisplayName("Any-permanent reduction applies when first target matches the predicate")
        void anyPermanentReductionApplies() {
            var predicate = new com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate();
            Card response = new Card();
            response.setName("Ajani's Response");
            response.setType(CardType.INSTANT);
            response.setManaCost("{4}{W}");
            response.addEffect(EffectSlot.STATIC,
                    new com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect(predicate, 3));

            Card bearCard = new Card();
            bearCard.setName("Bear");
            bearCard.setType(CardType.CREATURE);
            Permanent tappedBear = new Permanent(bearCard);
            tappedBear.tap();
            gd.playerBattlefields.get(player2Id).add(tappedBear);

            when(gameQueryService.findPermanentById(gd, tappedBear.getId())).thenReturn(tappedBear);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, tappedBear, predicate)).thenReturn(true);

            assertThat(svc.computeTargetBasedCostReduction(gd, player1Id, response, List.of(tappedBear.getId())))
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("Stack-entry reduction applies when first target is a matching spell on the stack")
        void stackEntryReductionApplies() {
            var predicate = new com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate(
                    Set.of(com.github.laxika.magicalvibes.model.StackEntryType.INSTANT_SPELL,
                            com.github.laxika.magicalvibes.model.StackEntryType.SORCERY_SPELL));
            Card brushOff = new Card();
            brushOff.setName("Brush Off");
            brushOff.setType(CardType.INSTANT);
            brushOff.setManaCost("{2}{U}{U}");
            brushOff.addEffect(EffectSlot.STATIC,
                    new com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect(predicate, 2));

            Card targetInstant = new Card();
            targetInstant.setName("Target Bolt");
            targetInstant.setType(CardType.INSTANT);
            var targetEntry = new com.github.laxika.magicalvibes.model.StackEntry(
                    com.github.laxika.magicalvibes.model.StackEntryType.INSTANT_SPELL,
                    targetInstant, player2Id, "Target Bolt", List.of(), 0, null, null);
            gd.stack.add(targetEntry);

            when(gameQueryService.findStackEntryByCardId(gd, targetInstant.getId())).thenReturn(targetEntry);
            when(predicateEvaluationService.matchesStackEntryPredicate(targetEntry, predicate, null)).thenReturn(true);

            assertThat(svc.computeTargetBasedCostReduction(gd, player1Id, brushOff, List.of(targetInstant.getId())))
                    .isEqualTo(2);
        }

        @Test
        @DisplayName("No reduction with empty target list")
        void noReductionWithoutTargets() {
            Card stomp = new Card();
            stomp.setName("Savage Stomp");
            stomp.setType(CardType.SORCERY);
            stomp.setManaCost("{2}{G}");
            stomp.addEffect(EffectSlot.STATIC,
                    new com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledPermanentEffect(
                            new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR), 2));

            assertThat(svc.computeTargetBasedCostReduction(gd, player1Id, stomp, List.of())).isZero();
        }
    }

    @Nested
    @DisplayName("getTargetingSubtypeTax")
    class TargetingSubtypeTax {

        @Test
        @DisplayName("Applies tax when opponent targets a matching controlled permanent")
        void appliesTaxForMatchingTarget() {
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);

            Card kopala = new Card();
            kopala.setName("Kopala, Warden of Waves");
            kopala.setType(CardType.CREATURE);
            kopala.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCostForTargetingControlledPermanentEffect(predicate, 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(kopala));

            Card merfolk = new Card();
            merfolk.setName("Merfolk");
            merfolk.setType(CardType.CREATURE);
            Permanent merfolkPermanent = new Permanent(merfolk);
            gd.playerBattlefields.get(player1Id).add(merfolkPermanent);

            when(gameQueryService.findPermanentById(gd, merfolkPermanent.getId())).thenReturn(merfolkPermanent);
            when(gameQueryService.findPermanentController(gd, merfolkPermanent.getId())).thenReturn(player1Id);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, merfolkPermanent, predicate)).thenReturn(true);

            assertThat(svc.getTargetingSubtypeTax(gd, player2Id, merfolkPermanent.getId(), null)).isEqualTo(2);
        }

        @Test
        @DisplayName("No tax when target does not match predicate")
        void noTaxWhenTargetDoesNotMatch() {
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);

            Card kopala = new Card();
            kopala.setName("Kopala, Warden of Waves");
            kopala.setType(CardType.CREATURE);
            kopala.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCostForTargetingControlledPermanentEffect(predicate, 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(kopala));

            Card bear = new Card();
            bear.setName("Grizzly Bears");
            bear.setType(CardType.CREATURE);
            Permanent bearPermanent = new Permanent(bear);
            gd.playerBattlefields.get(player1Id).add(bearPermanent);

            when(gameQueryService.findPermanentById(gd, bearPermanent.getId())).thenReturn(bearPermanent);
            when(gameQueryService.findPermanentController(gd, bearPermanent.getId())).thenReturn(player1Id);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, bearPermanent, predicate)).thenReturn(false);

            assertThat(svc.getTargetingSubtypeTax(gd, player2Id, bearPermanent.getId(), null)).isZero();
        }

        @Test
        @DisplayName("No tax when caster controls the taxing permanent")
        void noTaxWhenCasterControlsTaxSource() {
            var predicate = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);

            Card kopala = new Card();
            kopala.setName("Kopala, Warden of Waves");
            kopala.setType(CardType.CREATURE);
            kopala.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCostForTargetingControlledPermanentEffect(predicate, 2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(kopala));

            Card merfolk = new Card();
            merfolk.setName("Merfolk");
            merfolk.setType(CardType.CREATURE);
            Permanent merfolkPermanent = new Permanent(merfolk);
            gd.playerBattlefields.get(player1Id).add(merfolkPermanent);

            assertThat(svc.getTargetingSubtypeTax(gd, player1Id, merfolkPermanent.getId(), null)).isZero();
        }
    }
}
