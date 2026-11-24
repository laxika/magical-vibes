package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link AirbendTargetPermanentEffect} (CR 701.65a): the permanents its subject names leave the battlefield
 * together as one exile event, then a target spell is exiled, and one airbend event follows if anything was exiled
 * (CR 701.65b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirbendTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final SubjectResolutionService subjectResolutionService;
    private final ExileSupport exileSupport;
    private final ExileService exileService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AirbendTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var airbend = (AirbendTargetPermanentEffect) effect;
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, airbend.subject());
        boolean exiledAny = airbendPermanents(gameData, entry, permanents);
        if (airbend.subject().declaresTarget() && airbend.declaredTarget().admits(TargetPredicate.Kind.SPELL)) {
            for (UUID targetId : targetIds(entry, effect)) {
                if (gameQueryService.findPermanentById(gameData, targetId) == null) {
                    exiledAny |= airbendSpell(gameData, entry, targetId);
                }
            }
        }
        if (exiledAny) {
            ActionEvents.bent(gameData, entry.getControllerId(), BendingType.AIRBEND);
        }
    }

    private static Set<UUID> targetIds(StackEntry entry, CardEffect effect) {
        Set<UUID> ids = new LinkedHashSet<>(entry.targetsForEffect(effect));
        if (ids.isEmpty() && entry.getTargetId() != null) {
            ids.add(entry.getTargetId());
        }
        return ids;
    }

    /** Exiles {@code permanents} as one event (CR 603.10a) and grants each exiled card's owner its {2} cast. */
    private boolean airbendPermanents(GameData gameData, StackEntry entry, List<Permanent> permanents) {
        if (permanents.isEmpty()) {
            return false;
        }
        List<Card> exiledCards = new ArrayList<>();
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            gameData.runAsSingleCardExileBatch(() -> permanentRemovalService.performSimultaneousRemovals(
                    gameData, permanents, () -> {
                        for (Permanent permanent : permanents) {
                            List<Card> leaving = new ArrayList<>(permanent.cardsLeavingBattlefield());
                            if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                                exiledCards.addAll(leaving);
                            }
                        }
                    }));
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        for (Card exiledCard : exiledCards) {
            grantAirbentCast(gameData, entry, exiledCard);
        }
        return !exiledCards.isEmpty();
    }

    private void grantAirbentCast(GameData gameData, StackEntry entry, Card exiledCard) {
        UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
        if (ownerId != null && !exiledCard.hasType(CardType.LAND)) {
            exileSupport.grantCastWhileExiledForGenericCost(gameData, exiledCard.getId(), ownerId, 2);
            String ownerName = gameData.playerIdToName.get(ownerId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(exiledCard)
                    .text(" is airbent - " + ownerName
                            + " may cast it for {2} rather than its mana cost while it remains exiled.")
                    .build());
        } else {
            gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is airbent."));
        }
        log.info("Game {} - {} airbent by {} (owner may cast for {2})",
                gameData.id, exiledCard.getName(), entry.getCard().getName());
    }

    /**
     * Exiles the target spell and grants its owner the {2} cast; a copy ceases to exist and, as before the fold, raises
     * no airbend event.
     */
    private boolean airbendSpell(GameData gameData, StackEntry entry, UUID targetId) {
        StackEntry spell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (spell == null || spell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || spell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return false;
        }
        gameData.stack.remove(spell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);
        if (spell.isCopy()) {
            gameLogService.append(gameData, GameLog.cardThen(spell.getCard(), " (a copy) ceases to exist."));
            return false;
        }
        Card card = spell.getPhysicalCard();
        UUID ownerId = spell.getOwnerId();
        exileService.exileCard(gameData, ownerId, card);
        exileSupport.grantCastWhileExiledForGenericCost(gameData, card.getId(), ownerId, 2);
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is airbent by ", entry.getCard(), "."));
        log.info("Game {} - {} airbent the spell {}", gameData.id, entry.getCard().getName(), card.getName());
        return true;
    }
}
