package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSelectedPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token copy of a permanent selected by a preceding library choice. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSelectedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSelectedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfSelectedPermanentEffect) effect;
        StackEntry copyEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s selected permanent copy",
                java.util.List.of(),
                copyEffect.permanentId(),
                entry.getSourcePermanentId());
        copyEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        tokenCopyHandler.resolve(gameData, copyEntry, copyEffect.copyEffect());
        entry.getCreatedPermanentIds().addAll(copyEntry.getCreatedPermanentIds());
    }
}
