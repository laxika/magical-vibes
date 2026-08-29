package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetCreatureOrSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.UUID;

/** Resolves airbend targeting either a creature on the battlefield or a spell on the stack. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirbendTargetCreatureOrSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AirbendSupport airbendSupport;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final StateTriggerService stateTriggerService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AirbendTargetCreatureOrSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            airbendSupport.airbend(gameData, entry, permanent);
            triggerCollectionService.checkBendingTriggers(gameData, entry.getControllerId(), BendingType.AIRBEND);
            return;
        }

        StackEntry spell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (spell == null || spell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || spell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " fizzles (target is no longer a creature or spell)."));
            return;
        }

        gameData.stack.remove(spell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);
        if (spell.isCopy()) {
            gameLogService.append(gameData, GameLog.cardThen(spell.getCard(), " (a copy) ceases to exist."));
            return;
        }

        Card card = spell.getPhysicalCard();
        UUID ownerId = spell.getOwnerId();
        exileService.exileCard(gameData, ownerId, card);
        exileSupport.grantCastWhileExiledForGenericCost(gameData, card.getId(), ownerId, 2);
        triggerCollectionService.checkBendingTriggers(gameData, entry.getControllerId(), BendingType.AIRBEND);
        gameLogService.append(gameData, GameLog.cardTextCard(card,
                " is airbent by ", entry.getCard(), "."));
        log.info("Game {} - {} airbent the spell {}", gameData.id, entry.getCard().getName(), card.getName());
    }
}
