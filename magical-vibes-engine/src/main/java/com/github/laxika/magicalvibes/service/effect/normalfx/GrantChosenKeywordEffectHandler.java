package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantChosenKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantChosenKeywordEffect) effect;
        Permanent recipient = gameQueryService.findPermanentById(gameData, resolveRecipientId(entry, e));
        if (recipient == null) {
            return;
        }

        playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), recipient.getId(), e.options());
    }

    /**
     * Self-scoped abilities have no target and resolve against the source permanent, falling back to
     * {@code targetId} for the trigger shapes that carry the source there instead.
     */
    private UUID resolveRecipientId(StackEntry entry, GrantChosenKeywordEffect e) {
        if (e.scope() == GrantScope.SELF) {
            return entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        }
        return entry.getTargetId();
    }
}
