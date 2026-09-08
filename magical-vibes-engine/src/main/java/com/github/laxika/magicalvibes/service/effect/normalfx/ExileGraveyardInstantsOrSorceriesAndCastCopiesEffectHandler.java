package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Exiles every targeted instant or sorcery card from its graveyard, then casts a copy of each without
 * paying its mana cost (Spelltwine).
 *
 * <p>The copies go through {@link ExileFreeCastQueueSupport} so a copy that needs a target pauses for
 * the choice and the remaining copies are still cast afterwards. A target that left its graveyard is
 * skipped (CR 608.2b); the spell's own exile rider is handled by {@code ExileSpellEffect}.</p>
 */
@Component
@RequiredArgsConstructor
public class ExileGraveyardInstantsOrSorceriesAndCastCopiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect copyEffect =
                (ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect) effect;

        List<UUID> copyIds = new ArrayList<>();
        List<UUID> targetCardIds = entry.targetsForEffect(effect);
        if (targetCardIds.isEmpty()) {
            targetCardIds = entry.getTargetCardIds();
        }
        for (UUID targetCardId : targetCardIds) {
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null) {
                continue;
            }
            if (!targetCard.hasType(CardType.INSTANT) && !targetCard.hasType(CardType.SORCERY)) {
                continue;
            }
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
            if (graveyardOwnerId == null) {
                continue;
            }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
            gameLogService.append(gameData, GameLog.isExiled(targetCard));

            Card copy = copySupport.createCopyCard(targetCard);
            exileService.exileCard(gameData, controllerId, copy);
            copyIds.add(copy.getId());
        }

        if (copyEffect.mayCastCopies()) {
            for (int i = copyIds.size() - 1; i >= 0; i--) {
                UUID copyId = copyIds.get(i);
                Card copy = gameQueryService.findCardInExileById(gameData, copyId);
                if (copy != null) {
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            copy,
                            controllerId,
                            List.of(copyEffect),
                            "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                            copyId
                    ));
                }
            }
        } else if (!copyIds.isEmpty()) {
            exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, controllerId, copyIds);
        }
    }
}
