package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeToSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnCleanupServiceTest {

    @Test
    void cleanupPreservesUpcomingUntapRestrictions() {
        var untapping = new com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect(
                com.github.laxika.magicalvibes.model.effect.TapUntapScope.TARGET_PLAYERS_PERMANENTS, null, 1, true);
        gd.addFloatingEffect(com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect.onPlayer("Test", null,
                player1Id, untapping, player1Id, com.github.laxika.magicalvibes.model.effect.Duration.PERMANENT));

        sut.resetEndOfTurnModifiers(gd);

        assertThat(gd.nextUntapStepRestrictions(player1Id)).extracting(fe -> fe.effect()).containsExactly(untapping);
    }

    @Mock
    private CreatureControlService creatureControlService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService triggeredAbilityStackService;

    @InjectMocks
    private TurnCleanupService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(sut, "gameQueryService", gameQueryService);
        // The stack door puts a delayed ability on the stack (its own routes are TriggeredAbilityStackServiceTest's).
        org.springframework.test.util.ReflectionTestUtils.setField(sut, "triggeredAbilityStackService",
                triggeredAbilityStackService);
        org.mockito.Mockito.lenient().doAnswer(invocation -> {
            com.github.laxika.magicalvibes.testutil.TestStackDoor.enqueue(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(triggeredAbilityStackService).putDelayed(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
    }

    private static Card createCardWithName(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    @Nested
    @DisplayName("applyCleanupResets")
    class ApplyCleanupResets {

        @Test
        @DisplayName("Resets end-of-turn modifiers and returns stolen creatures")
        void delegatesToResetAndReturnStolen() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.applyCleanupResets(gd);

            assertThat(perm.getPowerModifier()).isZero();
            verify(creatureControlService).reconcileControl(gd);
        }

        @Test
        @DisplayName("Sheds counters scheduled for the cleanup step, keeping pre-existing ones")
        void shedsCountersScheduledForCleanup() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
            perm.getCountersToRemoveAtNextCleanup().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.applyCleanupResets(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(perm.getCountersToRemoveAtNextCleanup()).isEmpty();
        }

        @Test
        @DisplayName("Clamps the shed at the counters the permanent still has")
        void clampsShedToRemainingCounters() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            perm.getCountersToRemoveAtNextCleanup().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.applyCleanupResets(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }

    @Nested
    @DisplayName("resetEndOfTurnModifiers")
    class ResetEndOfTurnModifiers {

        @Test
        @DisplayName("Clears power and toughness modifiers on permanents")
        void clearsPowerToughnessModifiers() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(3);
            perm.setToughnessModifier(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }

        @Test
        @DisplayName("Clears marked damage on all permanents (CR 514.2)")
        void clearsMarkedDamage() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(4);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Clears granted keywords on permanents")
        void clearsGrantedKeywords() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.getGrantedKeywords().add(Keyword.FLYING);
            perm.getGrantedKeywords().add(Keyword.TRAMPLE);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getGrantedKeywords()).isEmpty();
        }

        @Test
        @DisplayName("Clears protection from colors on permanents whose only modifier is that protection")
        void clearsProtectionFromColors() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.getProtectionFromColorsUntilEndOfTurn().add(com.github.laxika.magicalvibes.model.CardColor.RED);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        }

        @Test
        @DisplayName("Clears basePowerToughnessOverriddenUntilEndOfTurn flag on permanents")
        void clearsBasePowerToughnessOverridden() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears losesAllAbilitiesUntilEndOfTurn flag on permanents")
        void clearsLosesAllAbilitiesUntilEndOfTurn() {
            Card card = createCardWithName("Alloy Myr");
            Permanent perm = new Permanent(card);
            perm.setLosesAllAbilitiesUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isLosesAllAbilitiesUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears losesAllAbilitiesUntilEndOfTurn even when no other modifiers are set")
        void clearsLosesAllAbilitiesAlone() {
            Card card = createCardWithName("Alloy Myr");
            Permanent perm = new Permanent(card);
            // Only set losesAllAbilities — no power/toughness/keyword modifiers
            perm.setLosesAllAbilitiesUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isLosesAllAbilitiesUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears temporary global triggered abilities")
        void clearsTemporaryGlobalTriggeredAbilities() {
            gd.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                    player1Id, createCardWithName("Shriveling Rot"),
                    new TriggeredAbility(Triggers.creatureDealtDamage(), new LoseLifeEffect(1))));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
        }

        @Test
        @DisplayName("Ends a \"when you next ... this turn\" delayed ability that hasn't triggered")
        void clearsNextDelayedAbilityThatHasNotTriggered() {
            gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(player1Id,
                    createCardWithName("Teach by Example"),
                    new TriggeredAbility(Triggers.youCastSpell(), new LoseLifeEffect(1)), null));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
        }

        @Test
        @DisplayName("Keeps a delayed ability with no stated duration while the permanent it is about remains")
        void keepsUntilTriggeredDelayedAbilityAboutAPermanentStillOnTheBattlefield() {
            Permanent watched = new Permanent(createCardWithName("Merieke Ri Berit"));
            Permanent gone = new Permanent(createCardWithName("Coffin Queen"));
            TemporaryGlobalTriggeredAbility kept = TemporaryGlobalTriggeredAbility.about(player1Id,
                    watched.getCard(), new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                            new LoseLifeEffect(1)), watched.getId(), watched.getId(), false);
            TemporaryGlobalTriggeredAbility orphaned = TemporaryGlobalTriggeredAbility.about(player1Id,
                    gone.getCard(), new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                            new LoseLifeEffect(1)), gone.getId(), gone.getId(), false);
            gd.temporaryGlobalTriggeredAbilities.add(kept);
            gd.temporaryGlobalTriggeredAbilities.add(orphaned);
            when(gameQueryService.findPermanentById(gd, watched.getId())).thenReturn(watched);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(kept);
        }

        @Test
        @DisplayName("Clears combatDamageExemptPredicate")
        void clearsCombatDamageExemptPredicate() {
            gd.combatDamageExemptPredicate = new com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate();

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.combatDamageExemptPredicate).isNull();
        }

        @Test
        @DisplayName("The cleanup step's damage removal is the rules event at which \"until end of turn\" effects end")
        void emitsTheCleanupEvent() {
            gd.activePlayerId = player1Id;

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.drainRulesEvents()).flatExtracting(RulesEventBatch::events)
                    .contains(new TurnEvent.Cleanup(player1Id));
        }

        @Test
        @DisplayName("Does not affect permanents without modifiers")
        void doesNotAffectUnmodifiedPermanents() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }
    }

    @Nested
    @DisplayName("drainManaPools")
    class DrainManaPools {

        @Test
        @DisplayName("Empties all players' mana pools")
        void emptiesAllManaPools() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 3);
            gd.playerManaPools.get(player2Id).add(ManaColor.BLUE, 2);

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isZero();
            assertThat(gd.playerManaPools.get(player2Id).getTotal()).isZero();
        }

        @Test
        @DisplayName("Does not drain mana pools when PreventManaDrainEffect is on the battlefield")
        void doesNotDrainWhenPreventManaDrainPresent() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 5);
            Card card = createCardWithName("Upwelling");
            card.addStaticEffect(new PreventManaDrainEffect());
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(5);
        }

        @Test
        @DisplayName("Preserves only the controller's protected color")
        void preservesControllerScopedColor() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 3);
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 2);
            gd.playerManaPools.get(player2Id).add(ManaColor.GREEN, 4);
            Card card = createCardWithName("Omnath, Locus of Mana");
            card.addStaticEffect(new PreventManaDrainEffect(ManaColor.GREEN));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(3);
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.RED)).isZero();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isZero();
        }

        @Test
        @DisplayName("Preserves a color from a player-scoped floating effect")
        void preservesPlayerScopedFloatingColor() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 3);
            gd.playerManaPools.get(player2Id).add(ManaColor.RED, 2);
            gd.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), "The Last Agni Kai", null, player1Id,
                    new PreventManaDrainEffect(ManaColor.RED), null, player1Id, null,
                    Duration.UNTIL_END_OF_TURN, 0));

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.RED)).isEqualTo(3);
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("Does nothing when mana pools are already empty")
        void doesNothingWhenAlreadyEmpty() {
            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("getMaxHandSize")
    class GetMaxHandSize {

        @Test
        @DisplayName("Returns 7 by default")
        void returnsSevenByDefault() {
            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Includes a floating controller maximum hand-size reduction")
        void includesFloatingControllerReduction() {
            gd.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), "Inspired Idea", null, player1Id,
                    new ReduceControllerMaxHandSizeEffect(3), null, player1Id, null,
                    Duration.PERMANENT, 0));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("Reduces hand size when opponent controls ReduceOpponentMaxHandSizeEffect")
        void reducedByOpponentReduceEffect() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(7));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(0);
        }

        @Test
        @DisplayName("Does not reduce own controller's hand size")
        void doesNotReduceOwnHandSize() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(7));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Multiple reduction effects from the same opponent stack additively")
        void multipleReductionEffectsStack() {
            Card card1 = createCardWithName("Reducer A");
            card1.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(2));
            Card card2 = createCardWithName("Reducer B");
            card2.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(3));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card2));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(2);
        }

        @Test
        @DisplayName("Sets own hand size when controlling SetControllerMaximumHandSizeEffect")
        void setByControllerSetEffect() {
            Card card = createCardWithName("Recycle");
            card.addStaticEffect(new SetControllerMaximumHandSizeEffect(2));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(2);
            assertThat(sut.getMaxHandSize(gd, player2Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Sets own hand size to the source permanent's current counter count")
        void setByControllerSourceCounterEffect() {
            Card card = createCardWithName("Midnight Oil");
            card.addStaticEffect(new SetControllerMaximumHandSizeToSourceCountersEffect(CounterType.HOUR));
            Permanent permanent = new Permanent(card);
            permanent.setCounterCount(CounterType.HOUR, 3);
            gd.playerBattlefields.get(player1Id).add(permanent);

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(3);

            permanent.setCounterCount(CounterType.HOUR, 0);
            assertThat(sut.getMaxHandSize(gd, player1Id)).isZero();
        }

        @Test
        @DisplayName("Sets hand size to a specific value when opponent controls SetOpponentMaximumHandSizeEffect")
        void setByOpponentSetEffect() {
            Card card = createCardWithName("Cursed Rack");
            card.addStaticEffect(new SetOpponentMaximumHandSizeEffect(4));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("Sets opponents' hand size from the controller's graveyard card types when delirium is met")
        void setByControllerGraveyardCardTypes() {
            Card winter = createCardWithName("Winter, Misanthropic Guide");
            winter.addStaticEffect(new SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect());
            gd.playerBattlefields.get(player2Id).add(new Permanent(winter));

            gd.playerGraveyards.put(player2Id, List.of(
                    cardOfType(CardType.CREATURE),
                    cardOfType(CardType.LAND),
                    cardOfType(CardType.INSTANT),
                    cardOfType(CardType.ENCHANTMENT)));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(3);
        }

        private Card cardOfType(CardType type) {
            Card card = new Card();
            card.setType(type);
            return card;
        }

        @Test
        @DisplayName("A set effect does not affect its own controller")
        void setEffectDoesNotAffectOwnController() {
            Card card = createCardWithName("Cursed Rack");
            card.addStaticEffect(new SetOpponentMaximumHandSizeEffect(4));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("A later set effect overrides an earlier reduction (timestamp order)")
        void setAppliedAfterReduceOverridesIt() {
            Card reducer = createCardWithName("Jin-Gitaxias, Core Augur");
            reducer.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(3));
            Card rack = createCardWithName("Cursed Rack");
            rack.addStaticEffect(new SetOpponentMaximumHandSizeEffect(4));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));
            gd.playerBattlefields.get(player2Id).add(new Permanent(rack));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("A reduction after a set is applied on top of the set value (timestamp order)")
        void reduceAppliedAfterSet() {
            Card rack = createCardWithName("Cursed Rack");
            rack.addStaticEffect(new SetOpponentMaximumHandSizeEffect(4));
            Card reducer = createCardWithName("Jin-Gitaxias, Core Augur");
            reducer.addStaticEffect(new ReduceOpponentMaxHandSizeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(rack));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("hasNoMaximumHandSize")
    class HasNoMaximumHandSize {

        @Test
        @DisplayName("Returns false by default")
        void returnsFalseByDefault() {
            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns true when player controls NoMaximumHandSizeEffect")
        void returnsTrueWithNoMaxHandSizeEffect() {
            Card card = createCardWithName("Spellbook");
            card.addStaticEffect(new NoMaximumHandSizeEffect());
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(permanent);
            lenient().when(gameQueryService.getActiveStaticEffects(gd, permanent, player1Id))
                    .thenReturn(card.getStaticEffects());

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Returns true when a permanent is granted NoMaximumHandSizeEffect")
        void returnsTrueWithGrantedNoMaxHandSizeEffect() {
            Permanent permanent = new Permanent(createCardWithName("Enchanted creature"));
            gd.playerBattlefields.get(player1Id).add(permanent);
            GameQueryService.StaticBonus bonus = new GameQueryService.StaticBonus(
                    0, 0, Set.of(), Set.of(), false, List.of(), List.of(new NoMaximumHandSizeEffect()),
                    Set.of(), List.of(), Set.of(), Set.of(), false, false, false, false,
                    Set.of(), false, null, null, false, false);
            when(gameQueryService.computeStaticBonus(gd, permanent)).thenReturn(bonus);

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Returns true when the player has a rest-of-game no-maximum-hand-size grant")
        void returnsTrueWhenInGlobalSet() {
            gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1Id,
                    new GrantNoMaximumHandSizeEffect(Duration.PERMANENT), player1Id,
                    Duration.PERMANENT));

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Opponent's NoMaximumHandSizeEffect does not affect this player")
        void opponentEffectDoesNotAffect() {
            Card card = createCardWithName("Spellbook");
            card.addStaticEffect(new NoMaximumHandSizeEffect());
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns true for both players when any battlefield has PlayersHaveNoMaximumHandSizeEffect")
        void playersHaveNoMaximumHandSizeAffectsEveryone() {
            Card card = createCardWithName("Anvil of Bogardan");
            card.addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(player2Id).add(permanent);
            lenient().when(gameQueryService.getActiveStaticEffects(gd, permanent, player2Id))
                    .thenReturn(card.getStaticEffects());

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
            assertThat(sut.hasNoMaximumHandSize(gd, player2Id)).isTrue();
        }
    }
}
