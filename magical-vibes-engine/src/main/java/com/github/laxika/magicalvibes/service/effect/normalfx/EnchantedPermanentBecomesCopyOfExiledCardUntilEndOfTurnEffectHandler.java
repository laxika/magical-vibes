package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a temporary copy effect for an Aura's enchanted permanent. */
@Component
@RequiredArgsConstructor
public class EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getAttachedTo() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        ExiledCardEntry exiled = gameData.getExiledWithPermanentEntries(
                        source.getId(), source.getCard().getId()).stream()
                .filter(candidate -> !candidate.faceDown())
                .findFirst()
                .orElse(null);
        if (enchanted == null || exiled == null) {
            return;
        }

        Card original = enchanted.getCard();
        if (!enchanted.isCopyUntilEndOfTurn()) {
            enchanted.setPreCopyCard(original);
        }
        EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect copyEffect =
                (EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect) effect;
        permanentCopierService.applyCloneCopy(
                enchanted, exiled.card(), null, null, copyEffect.additionalTypesOverride());
        enchanted.setCopyUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), enchanted.getId(), entry.getControllerId(),
                new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(), enchanted.getId(), null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData,
                GameLog.textCardText(original.getName() + " becomes a copy of ", exiled.card(),
                        " until end of turn."));
    }
}
