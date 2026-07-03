package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
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
        service = new BattlefieldEntryService(
                gameQueryService, gameBroadcastService, playerInputService,
                permanentCopierService, triggerCollectionService,
                graveyardTargetingService, etbTokenTargetService,
                new EtbEffectResolver(new ConditionEvaluationService(gameQueryService, predicateEvaluationService,
                        new StaticEffectSupport(gameQueryService, predicateEvaluationService))));

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

        when(gameBroadcastService.controlsPermanent(gd, player1Id, predicate)).thenReturn(false);

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

        when(gameBroadcastService.controlsPermanent(eq(gd), eq(player1Id), eq(predicate))).thenReturn(true);

        service.putPermanentOntoBattlefield(gd, player1Id, entering);

        assertThat(entering.isTapped()).isFalse();
    }
}
