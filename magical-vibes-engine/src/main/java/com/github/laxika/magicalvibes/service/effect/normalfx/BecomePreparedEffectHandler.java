package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BecomePreparedEffect}: the source permanent becomes prepared.
 *
 * <p>Creates a copy of the source's prepare spell (its {@code backFaceCard}) in exile with a
 * non-expiring play permission for the controller, links it to the source permanent, and marks the
 * permanent prepared. A no-op if the permanent is already prepared (CR-style: at most one prepare
 * copy in exile at a time) or has no prepare spell.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomePreparedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomePreparedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.isPrepared()) {
            return;
        }
        Card prepareSpell = source.getCard().getBackFaceCard();
        if (prepareSpell == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card copy = copySupport.createCopyCard(prepareSpell);
        exileService.exileCard(gameData, controllerId, copy);
        // No end-of-turn expiry: the prepare copy stays castable until it is cast or the prepared
        // permanent leaves the battlefield (PermanentRemovalService cleans it up in the latter case).
        gameData.exilePlayPermissions.put(copy.getId(), controllerId);

        source.setPrepared(true);
        source.setPreparedSpellCardId(copy.getId());

        String logEntry = source.getCard().getName() + " becomes prepared.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes prepared (prepare spell {} exiled)",
                gameData.id, source.getCard().getName(), copy.getName());
    }
}
