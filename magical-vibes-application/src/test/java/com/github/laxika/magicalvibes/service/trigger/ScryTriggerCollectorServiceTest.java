package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScryTriggerCollectorServiceTest {

    @Mock
    private GameLogService gameLogService;

    @InjectMocks
    private ScryTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gameData;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    @Test
    void queuesTheAtomicSelfTrigger() {
        Card card = new Card();
        card.setName("Flamespeaker Adept");
        Permanent permanent = new Permanent(card);
        SequenceEffect effect = SequenceEffect.of(
                new BoostSelfEffect(2, 0),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));

        boolean triggered = registry.dispatch(
                new TriggerMatchContext(gameData, permanent, playerId, effect),
                LibraryWatch.ON_CONTROLLER_SCRIES,
                new TriggerContext.Scry(playerId, 2, 3));

        assertThat(triggered).isTrue();
        assertThat(gameData.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getSourcePermanentId()).isEqualTo(permanent.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(2);
            assertThat(entry.getCardsLookedAtWhileScrying()).isEqualTo(3);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(playerId);
            assertThat(entry.isNonTargeting()).isFalse();
        });
        verify(gameLogService).append(eq(gameData), any());
    }

    @Test
    void queuesTargetedMayPayTriggerForTargetSelection() {
        Card card = new Card();
        card.setName("Knowledge and Power");
        Permanent permanent = new Permanent(card);
        MayPayManaEffect effect = new MayPayManaEffect(
                "{2}",
                new DealDamageToAnyTargetEffect(2),
                "Pay {2}?");

        boolean triggered = registry.dispatch(
                new TriggerMatchContext(gameData, permanent, playerId, effect),
                LibraryWatch.ON_CONTROLLER_SCRIES,
                new TriggerContext.Scry(playerId));

        assertThat(triggered).isTrue();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class))
                .satisfies(pending -> {
                    assertThat(pending.sourcePermanentId()).isEqualTo(permanent.getId());
                    assertThat(pending.effects()).containsExactly(effect);
                    assertThat(pending.targetFilter()).isInstanceOf(AnyTargetPredicateTargetFilter.class);
                    assertThat(pending.triggeringPlayerId()).isEqualTo(playerId);
                    assertThat(pending.eventValue()).isZero();
                });
    }

    @Test
    void preservesOncePerTurnMarkUntilTheMayChoiceIsAccepted() {
        Card card = new Card();
        card.setName("Planetarium of Wan Shi Tong");
        Permanent permanent = new Permanent(card);
        BoostSelfEffect effect = new BoostSelfEffect(1, 0);

        boolean triggered = registry.dispatch(
                new TriggerMatchContext(gameData, permanent, playerId, List.of(effect), card, null, "once"),
                LibraryWatch.ON_CONTROLLER_SCRIES,
                new TriggerContext.Scry(playerId));

        assertThat(triggered).isTrue();
        assertThat(gameData.stack).singleElement().satisfies(entry -> {
            assertThat(entry.marksOnceEachTurnAction()).isTrue();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    void anOpponentsScryRecordsThatPlayerAsTheNonTargetingTarget() {
        UUID opponentId = UUID.randomUUID();
        Card card = new Card();
        card.setName("River Song");
        Permanent permanent = new Permanent(card);
        BoostSelfEffect effect = new BoostSelfEffect(1, 1);

        boolean triggered = registry.dispatch(
                new TriggerMatchContext(gameData, permanent, playerId, effect),
                LibraryWatch.ON_OPPONENT_SCRIES,
                new TriggerContext.Scry(opponentId, 1, 2));

        assertThat(triggered).isTrue();
        assertThat(gameData.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getTargetId()).isEqualTo(opponentId);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(opponentId);
            assertThat(entry.getEventValue()).isEqualTo(1);
        });
    }
}
