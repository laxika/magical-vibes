package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CastingPermissionServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;

    private CastingPermissionService svc;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    private static final GameQueryService.StaticBonus NO_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(),
            false, false, false, false, Set.of(), false, 0, 0, false, false);

    @BeforeEach
    void setUp() {
        svc = new CastingPermissionService(gameQueryService, predicateEvaluationService, conditionEvaluationService);

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
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    @Test
    @DisplayName("conditional graveyard-land permission applies only when its condition is met")
    void conditionalGraveyardLandPermission() {
        Card siege = new Card();
        SourceHasChosenMode sultai = new SourceHasChosenMode("Sultai");
        siege.addEffect(EffectSlot.STATIC,
                new ConditionalEffect(sultai, new PlayLandsFromGraveyardEffect()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(siege));

        when(conditionEvaluationService.isMet(eq(gd), eq(sultai), any())).thenReturn(false);
        assertThat(svc.canPlayLandsFromGraveyard(gd, player1Id)).isFalse();

        when(conditionEvaluationService.isMet(eq(gd), eq(sultai), any())).thenReturn(true);
        assertThat(svc.canPlayLandsFromGraveyard(gd, player1Id)).isTrue();
    }

    @Nested
    @DisplayName("canCastFromTopOfLibrary")
    class CastFromTopOfLibrary {

        @Test
        @DisplayName("allows matching artifact and colorless spells")
        void allowsMatchingArtifactAndColorlessSpells() {
            Card forge = new Card();
            forge.addEffect(EffectSlot.STATIC,
                    new AllowCastFromTopOfLibraryEffect(Set.of(CardType.ARTIFACT), true));
            gd.playerBattlefields.get(player1Id).add(new Permanent(forge));

            Card coloredArtifact = new Card();
            coloredArtifact.setType(CardType.ARTIFACT);
            coloredArtifact.setColors(List.of(CardColor.RED));
            Card colorlessInstant = new Card();
            colorlessInstant.setType(CardType.INSTANT);

            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, coloredArtifact)).isTrue();
            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, colorlessInstant)).isTrue();
        }

        @Test
        @DisplayName("does not treat colored or land cards as colorless spells")
        void rejectsColoredAndLandCardsAsColorlessSpells() {
            Card forge = new Card();
            forge.addEffect(EffectSlot.STATIC,
                    new AllowCastFromTopOfLibraryEffect(Set.of(CardType.ARTIFACT), true));
            gd.playerBattlefields.get(player1Id).add(new Permanent(forge));

            Card coloredInstant = new Card();
            coloredInstant.setType(CardType.INSTANT);
            coloredInstant.setColors(List.of(CardColor.BLUE));
            Card colorlessLand = new Card();
            colorlessLand.setType(CardType.LAND);

            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, coloredInstant)).isFalse();
            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, colorlessLand)).isFalse();
        }

        @Test
        @DisplayName("allows cards matching a predicate filter")
        void allowsPredicateMatchingCard() {
            Card snoop = new Card();
            snoop.addEffect(EffectSlot.STATIC,
                    new AllowCastFromTopOfLibraryEffect(new CardSubtypePredicate(CardSubtype.GOBLIN)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(snoop));

            Card goblin = new Card();
            goblin.setType(CardType.CREATURE);
            when(predicateEvaluationService.matchesCardPredicate(
                    eq(goblin), any(CardPredicate.class), any(UUID.class), eq(gd), eq(player1Id)))
                    .thenReturn(true);

            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, goblin)).isTrue();
        }

        @Test
        @DisplayName("turn-scoped top-library permission allows spells and lands")
        void allowsSpellsAndLandsFromTopOfLibrary() {
            gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn.add(player1Id);

            Card instant = new Card();
            instant.setType(CardType.INSTANT);
            Card land = new Card();
            land.setType(CardType.LAND);

            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, instant)).isTrue();
            assertThat(svc.canPlayLandsFromTopOfLibrary(gd, player1Id)).isTrue();
            assertThat(svc.canCastFromTopOfLibrary(gd, player2Id, instant)).isFalse();
            assertThat(svc.canCastFromTopOfLibrary(gd, player1Id, land)).isFalse();
        }
    }

    @Test
    @DisplayName("stash counters grant turn-limited any-mana exile casting without source tracking")
    void stashCountersGrantExileCastingPermission() {
        Card tinybones = new Card();
        tinybones.addEffect(EffectSlot.STATIC,
                AllowCastFromCardsExiledWithSourceEffect.forStashCounters(true));
        gd.playerBattlefields.get(player1Id).add(new Permanent(tinybones));

        Card stashed = new Card();
        gd.addToExile(player2Id, stashed);
        gd.stashCounterCardIds.add(stashed.getId());

        assertThat(svc.hasCastFromExiledWithSourcePermission(gd, player1Id, stashed.getId())).isTrue();
        assertThat(svc.hasAnyManaTypePermission(gd, player1Id, stashed.getId())).isTrue();
        assertThat(svc.hasCastFromExiledWithSourcePermission(gd, player2Id, stashed.getId())).isFalse();
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
    @DisplayName("isSpellCastingAllowed — restrictions and limits")
    class RestrictionsAndLimits {

        @Test
        @DisplayName("Rejects spell when per-turn spell limit is reached")
        void rejectsWhenSpellLimitReached() {
            Card ruleOfLaw = new Card();
            ruleOfLaw.setName("Rule of Law");
            ruleOfLaw.setType(CardType.ENCHANTMENT);
            ruleOfLaw.addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1, SpellLimitScope.EACH_PLAYER));
            gd.playerBattlefields.get(player2Id).add(new Permanent(ruleOfLaw));

            Card dummy = new Card();
            dummy.setName("Dummy");
            dummy.setType(CardType.INSTANT);
            gd.recordSpellCast(player1Id, dummy);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, bolt)).isFalse();
        }

        @Test
        @DisplayName("Rejects spell of a restricted type")
        void rejectsRestrictedSpellType() {
            Card restrictor = new Card();
            restrictor.setName("Restrictor");
            restrictor.setType(CardType.ENCHANTMENT);
            restrictor.addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(restrictor));

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, creature)).isFalse();
        }

        @Test
        @DisplayName("Controller-only restriction on an opponent's permanent does not restrict this player")
        void controllerOnlyRestrictionIsNotSymmetric() {
            Card restrictor = new Card();
            restrictor.setName("Steel Golem");
            restrictor.setType(CardType.ARTIFACT);
            restrictor.addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)));
            gd.playerBattlefields.get(player2Id).add(new Permanent(restrictor));

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, creature)).isTrue();
        }

        @Test
        @DisplayName("Symmetric restriction on an opponent's permanent restricts this player too")
        void symmetricRestrictionAppliesToAllPlayers() {
            Card restrictor = new Card();
            restrictor.setName("Aether Storm");
            restrictor.setType(CardType.ENCHANTMENT);
            restrictor.addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE), true));
            gd.playerBattlefields.get(player2Id).add(new Permanent(restrictor));

            Card creature = new Card();
            creature.setName("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, creature)).isFalse();
        }

        @Test
        @DisplayName("Controller-only noncreature restriction does not affect an opponent")
        void controllerOnlyNoncreatureRestriction() {
            Card nullhide = new Card();
            nullhide.addEffect(EffectSlot.STATIC,
                    new NoncreatureSpellsCantBeCastEffect(0, false, false));
            gd.playerBattlefields.get(player1Id).add(new Permanent(nullhide));

            Card shock = new Card();
            shock.setType(CardType.INSTANT);
            shock.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, shock)).isFalse();
            assertThat(svc.isSpellCastingAllowed(gd, player2Id, shock)).isTrue();
        }

        @Test
        @DisplayName("Rejects an opponent's spell of a permanent's chosen color")
        void rejectsOpponentSpellOfChosenColor() {
            Card iona = new Card();
            iona.setName("Iona, Shield of Emeria");
            iona.setType(CardType.CREATURE);
            iona.addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsOfChosenColorEffect());
            Permanent ionaPerm = new Permanent(iona);
            ionaPerm.setChosenColor(CardColor.RED);
            gd.playerBattlefields.get(player1Id).add(ionaPerm);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setColors(List.of(CardColor.RED));
            bolt.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player2Id, bolt)).isFalse();
        }

        @Test
        @DisplayName("Allows an opponent's spell of another color")
        void allowsOpponentSpellOfAnotherColor() {
            Card iona = new Card();
            iona.setName("Iona, Shield of Emeria");
            iona.setType(CardType.CREATURE);
            iona.addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsOfChosenColorEffect());
            Permanent ionaPerm = new Permanent(iona);
            ionaPerm.setChosenColor(CardColor.RED);
            gd.playerBattlefields.get(player1Id).add(ionaPerm);

            Card snag = new Card();
            snag.setName("Vapor Snag");
            snag.setType(CardType.INSTANT);
            snag.setColors(List.of(CardColor.BLUE));
            snag.setManaCost("{U}");

            assertThat(svc.isSpellCastingAllowed(gd, player2Id, snag)).isTrue();
        }

        @Test
        @DisplayName("Does not restrict the source controller")
        void doesNotRestrictSourceController() {
            Card iona = new Card();
            iona.setName("Iona, Shield of Emeria");
            iona.setType(CardType.CREATURE);
            iona.addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsOfChosenColorEffect());
            Permanent ionaPerm = new Permanent(iona);
            ionaPerm.setChosenColor(CardColor.RED);
            gd.playerBattlefields.get(player1Id).add(ionaPerm);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setColors(List.of(CardColor.RED));
            bolt.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, bolt)).isTrue();
        }

        @Test
        @DisplayName("Rejects spell with a forbidden chosen name")
        void rejectsForbiddenCardName() {
            Card namer = new Card();
            namer.setName("Meddling Mage");
            namer.setType(CardType.CREATURE);
            namer.addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect());
            Permanent namerPerm = new Permanent(namer);
            namerPerm.setChosenName("Lightning Bolt");
            gd.playerBattlefields.get(player2Id).add(namerPerm);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, bolt)).isFalse();
        }

        @Test
        @DisplayName("Restricts opponents from casting a name tracked as exiled with the source")
        void rejectsOpponentSpellNamedLikeTrackedExile() {
            Card godsend = new Card();
            godsend.setName("Godsend");
            godsend.addEffect(EffectSlot.STATIC,
                    new CantCastSpellsWithSameNameAsExiledCardEffect(true));
            Permanent godsendPermanent = new Permanent(godsend);
            gd.playerBattlefields.get(player1Id).add(godsendPermanent);

            Card exiled = new Card();
            exiled.setName("Lightning Bolt");
            gd.addToExile(player1Id, exiled, godsendPermanent.getId());

            assertThat(svc.getForbiddenCardNames(gd, player2Id)).contains("Lightning Bolt");
            assertThat(svc.getForbiddenCardNames(gd, player1Id)).doesNotContain("Lightning Bolt");
        }

        @Test
        @DisplayName("Rejects spell when player is silenced this turn")
        void rejectsWhenSilenced() {
            gd.playersSilencedThisTurn.add(player1Id);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, bolt)).isFalse();
        }
    }

    @Nested
    @DisplayName("canCastWithCastCondition — card-level cast gate")
    class CastConditionGate {

        @Test
        @DisplayName("Card with no cast condition always passes without evaluating anything")
        void noConditionPasses() {
            Card card = new Card();
            card.setName("Grizzly Bears");

            assertThat(svc.canCastWithCastCondition(gd, player1Id, card)).isTrue();
            verify(conditionEvaluationService, never()).isMet(any(), any(), any());
        }

        @Test
        @DisplayName("Delegates to ConditionEvaluationService when a cast condition is present")
        void delegatesWhenConditionPresent() {
            Card card = new Card();
            card.setName("Talara's Battalion");
            card.setCastCondition(new Morbid());

            when(conditionEvaluationService.isMet(eq(gd), eq(card.getCastCondition()), any()))
                    .thenReturn(false);
            assertThat(svc.canCastWithCastCondition(gd, player1Id, card)).isFalse();

            when(conditionEvaluationService.isMet(eq(gd), eq(card.getCastCondition()), any()))
                    .thenReturn(true);
            assertThat(svc.canCastWithCastCondition(gd, player1Id, card)).isTrue();
        }
    }

    @Nested
    @DisplayName("isGraveyardCastAvailable — graveyard-only condition")
    class GraveyardCastAvailability {

        @Test
        @DisplayName("Rejects a graveyard cast while its availability condition is unmet")
        void rejectsWhenAvailabilityConditionIsUnmet() {
            GraveyardCast graveyardCast = new GraveyardCast(new MaxSpeed());
            when(conditionEvaluationService.isMet(eq(gd), eq(new MaxSpeed()), any())).thenReturn(false);

            assertThat(svc.isGraveyardCastAvailable(gd, player1Id, graveyardCast)).isFalse();
        }

        @Test
        @DisplayName("Allows a graveyard cast when its availability condition is met")
        void allowsWhenAvailabilityConditionIsMet() {
            GraveyardCast graveyardCast = new GraveyardCast(new MaxSpeed());
            when(conditionEvaluationService.isMet(eq(gd), eq(new MaxSpeed()), any())).thenReturn(true);

            assertThat(svc.isGraveyardCastAvailable(gd, player1Id, graveyardCast)).isTrue();
        }
    }

    @Nested
    @DisplayName("flashTimingRequiresAlternateCast — flash belongs to the alternate cost")
    class FlashAlternateCastGate {

        private Card flashAlternateCastCreature() {
            Card card = new Card();
            card.setName("Harbinger of the Tides");
            card.setType(CardType.CREATURE);
            card.setManaCost("{U}{U}");
            card.addCastingOption(new AlternateHandCast(
                    List.of(new ManaCastingCost("{2}{U}{U}")), null, true));
            return card;
        }

        @Test
        @DisplayName("Normal-cost cast outside sorcery timing is rejected")
        void rejectsNormalCostAtInstantSpeed() {
            gd.activePlayerId = player2Id;

            assertThat(svc.flashTimingRequiresAlternateCast(gd, player1Id, flashAlternateCastCreature())).isTrue();
        }

        @Test
        @DisplayName("Normal-cost cast during your main phase is unaffected")
        void allowsNormalCostAtSorceryTiming() {
            assertThat(svc.flashTimingRequiresAlternateCast(gd, player1Id, flashAlternateCastCreature())).isFalse();
        }

        @Test
        @DisplayName("A card that already has flash needs no alternate cast")
        void cardWithFlashIsUnaffected() {
            gd.activePlayerId = player2Id;
            Card card = flashAlternateCastCreature();
            card.setKeywords(Set.of(Keyword.FLASH));

            assertThat(svc.flashTimingRequiresAlternateCast(gd, player1Id, card)).isFalse();
        }

        @Test
        @DisplayName("A card without a flash-granting alternate cast is unaffected")
        void cardWithoutFlashAlternateCastIsUnaffected() {
            gd.activePlayerId = player2Id;
            Card card = new Card();
            card.setName("Grizzly Bears");
            card.setType(CardType.CREATURE);
            card.setManaCost("{1}{G}");

            assertThat(svc.flashTimingRequiresAlternateCast(gd, player1Id, card)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasGraveyardPlayPermission — turn-scoped filtered grants")
    class GraveyardCastFilterPermissions {

        private Card zombieCard() {
            Card card = new Card();
            card.setName("Walking Corpse");
            card.setType(CardType.CREATURE);
            card.setManaCost("{1}{B}");
            return card;
        }

        @Test
        @DisplayName("A matching filter grant permits the cast")
        void filterGrantPermitsCast() {
            Card card = zombieCard();
            CardPredicate filter = new CardSubtypePredicate(CardSubtype.ZOMBIE);
            gd.graveyardCastFilterPermissionsThisTurn.add(
                    new GameData.GraveyardCastFilterPermission(player1Id, filter));
            when(predicateEvaluationService.matchesCardPredicate(card, filter, null)).thenReturn(true);

            assertThat(svc.hasGraveyardPlayPermission(gd, card, player1Id)).isTrue();
        }

        @Test
        @DisplayName("A non-matching card is not covered by the grant")
        void nonMatchingCardIsRejected() {
            Card card = zombieCard();
            CardPredicate filter = new CardSubtypePredicate(CardSubtype.ZOMBIE);
            gd.graveyardCastFilterPermissionsThisTurn.add(
                    new GameData.GraveyardCastFilterPermission(player1Id, filter));
            when(predicateEvaluationService.matchesCardPredicate(card, filter, null)).thenReturn(false);

            assertThat(svc.hasGraveyardPlayPermission(gd, card, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Another player's grant does not apply")
        void grantIsPerPlayer() {
            Card card = zombieCard();
            gd.graveyardCastFilterPermissionsThisTurn.add(new GameData.GraveyardCastFilterPermission(
                    player2Id, new CardSubtypePredicate(CardSubtype.ZOMBIE)));

            assertThat(svc.hasGraveyardPlayPermission(gd, card, player1Id)).isFalse();
        }

        @Test
        @DisplayName("A land is not a spell, so a filter grant never covers it")
        void landsAreNotCoveredByFilterGrants() {
            Card land = new Card();
            land.setName("Swamp");
            land.setType(CardType.LAND);
            gd.graveyardCastFilterPermissionsThisTurn.add(new GameData.GraveyardCastFilterPermission(
                    player1Id, new CardSubtypePredicate(CardSubtype.ZOMBIE)));

            assertThat(svc.hasGraveyardPlayPermission(gd, land, player1Id)).isFalse();
        }

        @Test
        @DisplayName("A per-card grant still permits the cast without any filter grant")
        void perCardGrantStillWorks() {
            Card card = zombieCard();
            gd.graveyardPlayPermissions.put(card.getId(), player1Id);

            assertThat(svc.hasGraveyardPlayPermission(gd, card, player1Id)).isTrue();
        }
    }

    @Nested
    @DisplayName("isGraveyardCastAvailable â€” cast-time condition")
    class GraveyardCastCondition {

        @Test
        @DisplayName("Rejects a graveyard cast when its condition is not met")
        void rejectsWhenConditionIsNotMet() {
            GainedLifeThisTurn condition = new GainedLifeThisTurn();
            when(conditionEvaluationService.isMet(eq(gd), eq(condition), any())).thenReturn(false);

            assertThat(svc.isGraveyardCastAvailable(gd, player1Id, new GraveyardCast(condition))).isFalse();
        }

        @Test
        @DisplayName("Allows a graveyard cast when its condition is met")
        void allowsWhenConditionIsMet() {
            GainedLifeThisTurn condition = new GainedLifeThisTurn();
            when(conditionEvaluationService.isMet(eq(gd), eq(condition), any())).thenReturn(true);

            assertThat(svc.isGraveyardCastAvailable(gd, player1Id, new GraveyardCast(condition))).isTrue();
        }
    }
}
