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
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
                com.github.laxika.magicalvibes.model.effect.Subjects.controlledBy(com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayer()), 1, true);
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
    private AmountEvaluationService amountEvaluationService;

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
        org.springframework.test.util.ReflectionTestUtils.setField(sut, "amountEvaluationService", amountEvaluationService);
        // The hand-size amounts these tests use: a fixed number and the source's counters.
        lenient().when(amountEvaluationService.evaluate(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> switch (invocation.getArgument(1)) {
                    case Fixed fixed -> fixed.value();
                    case CountersOnSource counters ->
                            invocation.<AmountContext>getArgument(2).sourcePermanent().getCounterCount(counters.counterType());
                    default -> throw new IllegalArgumentException("Not stubbed: " + invocation.getArgument(1));
                });
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
        // Static abilities are the printed ones: nothing in these tests removes or grants abilities.
        lenient().when(gameQueryService.getActiveStaticEffects(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(Permanent.class), org.mockito.ArgumentMatchers.any(UUID.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().getStaticEffects());
        lenient().when(gameQueryService.activeStaticEffects(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(Class.class)))
                .thenAnswer(invocation -> activeStaticEffects(gd.orderedPlayerIds, invocation.getArgument(1)));
        lenient().when(gameQueryService.activeStaticEffects(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(UUID.class), org.mockito.ArgumentMatchers.any(Class.class)))
                .thenAnswer(invocation -> activeStaticEffects(List.of(invocation.<UUID>getArgument(1)),
                        invocation.getArgument(2)));
    }

    /** The static effects of {@code type} printed on the given players' permanents, as GameQueryService walks them. */
    private List<GameQueryService.ActiveStaticEffect<com.github.laxika.magicalvibes.model.effect.CardEffect>>
            activeStaticEffects(List<UUID> controllerIds, Class<?> type) {
        List<GameQueryService.ActiveStaticEffect<com.github.laxika.magicalvibes.model.effect.CardEffect>> found =
                new ArrayList<>();
        for (UUID controllerId : controllerIds) {
            for (Permanent permanent : gd.playerBattlefields.get(controllerId)) {
                permanent.getCard().getStaticEffects().stream()
                        .filter(type::isInstance)
                        .forEach(effect -> found.add(
                                new GameQueryService.ActiveStaticEffect<>(effect, permanent, controllerId)));
            }
        }
        return found;
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
            perm.setMarkedDamage(1);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.applyCleanupResets(gd);

            assertThat(perm.getMarkedDamage()).isZero();
            verify(creatureControlService).reconcileControl(gd);
        }
    }

    @Nested
    @DisplayName("resetEndOfTurnModifiers")
    class ResetEndOfTurnModifiers {

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

            assertThat(perm.getMarkedDamage()).isZero();
            assertThat(gd.floatingEffects).isEmpty();
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
                    SetControllerMaximumHandSizeEffect.reduce(Subjects.you(), 3), null, player1Id, null,
                    Duration.PERMANENT, 0));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("An opponent's \"each opponent's maximum hand size is reduced\" reduces the player's")
        void reducedByOpponentReduceEffect() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 7));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(0);
        }

        @Test
        @DisplayName("Does not reduce own controller's hand size")
        void doesNotReduceOwnHandSize() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 7));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Multiple reduction effects from the same opponent stack additively")
        void multipleReductionEffectsStack() {
            Card card1 = createCardWithName("Reducer A");
            card1.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 2));
            Card card2 = createCardWithName("Reducer B");
            card2.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 3));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card2));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(2);
        }

        @Test
        @DisplayName("\"Your maximum hand size is N\" sets only its controller's")
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
            card.addStaticEffect(SetControllerMaximumHandSizeEffect.set(Subjects.you(),
                    new CountersOnSource(CounterType.HOUR)));
            Permanent permanent = new Permanent(card);
            permanent.setCounterCount(CounterType.HOUR, 3);
            gd.playerBattlefields.get(player1Id).add(permanent);

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(3);

            permanent.setCounterCount(CounterType.HOUR, 0);
            assertThat(sut.getMaxHandSize(gd, player1Id)).isZero();
        }

        @Test
        @DisplayName("\"The chosen player's maximum hand size is four\" sets the chosen player's")
        void setByOpponentSetEffect() {
            gd.playerBattlefields.get(player2Id).add(cursedRackChoosing(player1Id));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
            assertThat(sut.getMaxHandSize(gd, player2Id)).isEqualTo(7);
        }

        private Permanent cursedRackChoosing(UUID chosenPlayerId) {
            Card card = createCardWithName("Cursed Rack");
            card.addStaticEffect(SetControllerMaximumHandSizeEffect.set(Subjects.chosenPlayer(), new Fixed(4)));
            Permanent rack = new Permanent(card);
            rack.setRememberedTargetPlayerId(chosenPlayerId);
            return rack;
        }

        @Test
        @DisplayName("A set effect does not affect its own controller")
        void setEffectDoesNotAffectOwnController() {
            gd.playerBattlefields.get(player1Id).add(cursedRackChoosing(player2Id));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("A later set effect overrides an earlier reduction (timestamp order)")
        void setAppliedAfterReduceOverridesIt() {
            Card reducer = createCardWithName("Jin-Gitaxias, Core Augur");
            reducer.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 3));
            gd.playerBattlefields.get(player2Id).add(new Permanent(reducer));
            gd.playerBattlefields.get(player2Id).add(cursedRackChoosing(player1Id));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("A reduction after a set is applied on top of the set value (timestamp order)")
        void reduceAppliedAfterSet() {
            Permanent rack = cursedRackChoosing(player1Id);
            Card reducer = createCardWithName("Jin-Gitaxias, Core Augur");
            reducer.addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 1));
            gd.playerBattlefields.get(player2Id).add(rack);
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
