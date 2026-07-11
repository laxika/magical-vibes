package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetControllerIfTargetHasKeywordEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetControllerIfTargetHasKeywordEffectHandler dealDamageToTargetControllerIfTargetHasKeywordHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetControllerIfTargetHasKeywordHandler = new DealDamageToTargetControllerIfTargetHasKeywordEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals bonus damage to controller when target creature has the keyword")
            void dealsBonusDamageWhenTargetHasKeyword() {
                Card burnCard = createCard("Burn the Impure");
                Permanent creature = addPermanent(player2Id, createCreature("Blightwidow", 2, 4));
                StackEntry entry = createEntry(burnCard, player1Id, creature.getId());
                DealDamageToTargetControllerIfTargetHasKeywordEffect effect =
                        new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT);

                stubNoDamageMultiplier();
                stubDamageFromSourceNotPrevented();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);
                when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
                when(gameQueryService.hasKeyword(gd, creature, Keyword.INFECT)).thenReturn(true);
                when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);

                dealDamageToTargetControllerIfTargetHasKeywordHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Does not deal bonus damage when target creature lacks the keyword")
            void noBonusDamageWhenTargetLacksKeyword() {
                Card burnCard = createCard("Burn the Impure");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                DealDamageToTargetControllerIfTargetHasKeywordEffect effect =
                        new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT);

                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INFECT)).thenReturn(false);

                dealDamageToTargetControllerIfTargetHasKeywordHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verifyNoInteractions(triggerCollectionService);
            }
}
