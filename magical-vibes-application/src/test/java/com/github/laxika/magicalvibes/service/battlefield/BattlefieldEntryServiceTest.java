package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattlefieldEntryServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentCopierService permanentCopierService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private GraveyardTargetingService graveyardTargetingService;
    @Mock private ETBTokenTargetService etbTokenTargetService;

    private BattlefieldEntryService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        PredicateEvaluationService predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        ConditionEvaluationService conditionEvaluationService = new ConditionEvaluationService(
                gameQueryService, predicateEvaluationService,
                new StaticEffectSupport(gameQueryService, predicateEvaluationService));
        service = new BattlefieldEntryService(
                gameQueryService, gameBroadcastService, castingCostService, playerInputService,
                permanentCopierService, triggerCollectionService,
                graveyardTargetingService, etbTokenTargetService,
                new EtbEffectResolver(conditionEvaluationService),
                new AmountEvaluationService(predicateEvaluationService, gameQueryService),
                conditionEvaluationService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("Enters tapped when controller does not control a matching permanent")
    void entersTappedWhenPredicateNotSatisfied() {
        var predicate = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SWAMP, CardSubtype.MOUNTAIN));
        Card land = new Card();
        land.setName("Dragonskull Summit");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.STATIC, new EntersTappedUnlessControlsPermanentEffect(predicate));
        Permanent entering = new Permanent(land);

        when(castingCostService.controlsPermanent(gd, player1Id, predicate)).thenReturn(false);

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when controller controls a matching permanent")
    void entersUntappedWhenPredicateSatisfied() {
        var predicate = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SWAMP, CardSubtype.MOUNTAIN));
        Card land = new Card();
        land.setName("Dragonskull Summit");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.STATIC, new EntersTappedUnlessControlsPermanentEffect(predicate));
        Permanent entering = new Permanent(land);

        when(castingCostService.controlsPermanent(eq(gd), eq(player1Id), eq(predicate))).thenReturn(true);

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.isTapped()).isFalse();
    }

    // ===== "Enters with … counters" replacement effects (CR 614.1c / 614.12) =====

    private Permanent enteringWithEffect(com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        Card card = new Card();
        card.setName("Entering Permanent");
        card.setType(CardType.ARTIFACT);
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        return new Permanent(card);
    }

    @Test
    @DisplayName("Enters with a fixed number of counters")
    void entersWithFixedCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with X counters read from the spell's cast context")
    void entersWithXCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        service.putPermanentOntoBattlefield(gd, player1Id, entering, 4, false);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters with 0 counters when not cast (X defaults to 0)")
    void entersWithZeroCountersWhenNotCast() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kicked conditional applies counters only when the spell was kicked")
    void kickedConditionalCounters() {
        Permanent kicked = enteringWithEffect(new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
        Permanent notKicked = enteringWithEffect(new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));

        service.putPermanentOntoBattlefield(gd, player1Id, kicked, 0, true);
        service.putPermanentOntoBattlefield(gd, player1Id, notKicked, 0, false);

        assertThat(kicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(notKicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raid conditional applies counters only when the controller attacked this turn")
    void raidConditionalCounters() {
        Permanent entering = enteringWithEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
        service.putPermanentOntoBattlefield(gd, player1Id, entering);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gd.playersDeclaredAttackersThisTurn.add(player1Id);
        Permanent enteringAfterRaid = enteringWithEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
        service.putPermanentOntoBattlefield(gd, player1Id, enteringAfterRaid);
        assertThat(enteringAfterRaid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with a counter for each creature that died this turn (all players)")
    void entersWithCreatureDeathCounters() {
        UUID player2Id = UUID.randomUUID();
        gd.creatureDeathCountThisTurn.put(player1Id, 2);
        gd.creatureDeathCountThisTurn.put(player2Id, 1);
        gd.orderedPlayerIds.add(player2Id);
        Permanent entering = enteringWithEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("CantHaveCountersEffect prevents enters-with counters")
    void cantHaveCountersPreventsEntersWithCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));
        entering.getCard().addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
