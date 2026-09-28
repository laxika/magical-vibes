package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a source creature's self-exile as a foretold card. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfAndBecomeForetoldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndBecomeForetoldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Card card = source.getCard();
        if (!permanentRemovalService.removePermanentToExileFaceDown(gameData, source)) {
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
        if (exiled == null) {
            return;
        }

        ManaCost foretellCost = card.getCastingOption(ForetellCast.class)
                .map(ForetellCast::manaCostString)
                .map(ManaCost::new)
                .orElse(null);
        if (!gameData.markExiledCardAsForetold(card.getId(), exiled.ownerId(), foretellCost)) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled face down and becomes foretold."));
        log.info("Game {} - {} is exiled face down and becomes foretold",
                gameData.id, card.getName());
    }
}
