package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GrantChosenKeywordEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantChosenKeywordEffect) effect;
        Permanent recipient = subjectResolutionService.permanent(gameData, entry, effect,
                e.scope() == GrantScope.SELF ? GrantScope.SELF.subject() : GrantScope.TARGET.subject());
        if (recipient == null) {
            return;
        }

        playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), recipient.getId(), e.options(),
                e.duration(), entry.getCard() == null ? null : entry.getCard().getName(),
                entry.getSourcePermanentId());
    }
}
