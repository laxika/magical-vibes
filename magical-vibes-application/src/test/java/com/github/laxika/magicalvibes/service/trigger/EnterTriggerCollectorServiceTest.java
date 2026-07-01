package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the enter-the-battlefield trigger orchestration ({@link TriggerCollectionService})
 * driving the {@link EnterTriggerCollectorService} handlers through a real
 * {@link TriggerCollectorRegistry} (mirrors {@code MiscTriggerCollectorServiceTest}'s setup).
 */
@ExtendWith(MockitoExtension.class)
class EnterTriggerCollectorServiceTest {

    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggeredAbilityQueueService triggeredAbilityQueueService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;

    private TriggerCollectionService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        TriggerCollectorRegistry registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameBroadcastService), registry);

        service = new TriggerCollectionService(registry, gameOutcomeService, playerInputService,
                triggeredAbilityQueueService, gameQueryService, gameBroadcastService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    private void addAllyCreatureTrigger(EffectSlot slot, com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        Card source = new Card();
        source.setName("Source");
        source.addEffect(slot, effect);
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));
    }

    private static Card enteringCreature(int power, int toughness) {
        Card entering = new Card();
        entering.setName("Entering Creature");
        entering.setType(CardType.CREATURE);
        entering.setPower(power);
        entering.setToughness(toughness);
        return entering;
    }

    @Test
    @DisplayName("Ally-creature scan skips non-creature (null toughness) entrants")
    void allyCreatureSkipsNonCreature() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));

        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, land, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature stat conditional gates below threshold")
    void allyCreatureConditionalGatesBelowThreshold() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2), 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature stat conditional queues the wrapped effect when met")
    void allyCreatureConditionalQueuesWhenMet() {
        Card source = new Card();
        source.setName("Garruk's Packleader");
        source.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(4, 4), 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(source);
    }

    @Test
    @DisplayName("Ally-creature scan does not trigger for the entering permanent itself")
    void allyCreatureDoesNotTriggerSelf() {
        Card entering = enteringCreature(4, 4);
        entering.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Naban doubling duplicates each ally-creature trigger produced during the scan")
    void allyCreatureNabanDoubling() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(4, 4), 1);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Any-creature scan queues a non-targeting trigger (Midnight Guard's untap)")
    void anyCreatureQueuesNonTargeting() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new UntapSelfEffect());

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(UntapSelfEffect.class);
    }

    @Test
    @DisplayName("Any-creature scan skips a generic targeting trigger")
    void anyCreatureSkipsTargeting() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new DestroyTargetPermanentEffect());

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Any-creature Soul Warden gain-life queues with no player target")
    void anyCreatureGainLife() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }
}
