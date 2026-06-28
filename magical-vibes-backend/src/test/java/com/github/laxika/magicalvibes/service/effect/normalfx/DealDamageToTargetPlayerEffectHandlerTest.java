package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BoostColorSourceDamageThisTurnEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageIfFewCardsInHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToAnyTargetAndGainLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToAnyTargetEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToControllerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetControllerIfTargetHasKeywordEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetPlayerByHandSizeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetPlayerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealOrderedDamageToAnyTargetsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealXDamageToAnyTargetAndGainXLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealXDamageToAnyTargetEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.FirstTargetDealsPowerDamageToSecondTargetEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.MassDamageEffectHandler;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetPlayerEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetPlayerEffectHandler dealDamageToTargetPlayerHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetPlayerHandler = new DealDamageToTargetPlayerEffectHandler(damageSupport, gameQueryService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals damage to target player")
            void dealsDamageToTargetPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                StackEntry entry = createEntry(lavaAxeCard, player1Id, player2Id);
                DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToTargetPlayerHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Does nothing when target is not a player")
            void doesNothingWhenTargetNotPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                UUID fakeId = UUID.randomUUID();
                StackEntry entry = createEntry(lavaAxeCard, player1Id, fakeId);
                DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

                dealDamageToTargetPlayerHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verifyNoInteractions(triggerCollectionService);
            }
}
