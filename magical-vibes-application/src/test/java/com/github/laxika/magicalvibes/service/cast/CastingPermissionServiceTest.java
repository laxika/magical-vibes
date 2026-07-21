package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.condition.Morbid;
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
            ruleOfLaw.addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1));
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
}
