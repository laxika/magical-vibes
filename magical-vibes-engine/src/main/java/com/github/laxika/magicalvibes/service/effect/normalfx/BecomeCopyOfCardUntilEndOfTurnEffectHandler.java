package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

/** Applies a temporary copy of a captured artifact or creature card. */
@Component
@RequiredArgsConstructor
public class BecomeCopyOfCardUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeCopyOfCardUntilEndOfTurnEffect copyEffect = (BecomeCopyOfCardUntilEndOfTurnEffect) effect;
        Card card = copyEffect.card();
        if (card == null || !card.hasType(CardType.CREATURE)) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (!source.isCopyUntilEndOfTurn()) {
            source.setPreCopyCard(source.getCard());
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, card, null, null,
                copyEffect.additionalTypesOverride());
        if (!copyEffect.additionalSubtypesOverride().isEmpty()) {
            ArrayList<CardSubtype> subtypes = new ArrayList<>(source.getCard().getSubtypes());
            for (CardSubtype subtype : copyEffect.additionalSubtypesOverride()) {
                if (!subtypes.contains(subtype)) {
                    subtypes.add(subtype);
                }
            }
            source.getCard().setSubtypes(subtypes);
        }
        source.setCopyUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(),
                entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(), source.getId(),
                null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + card.getName() + " until end of turn."));
    }
}
