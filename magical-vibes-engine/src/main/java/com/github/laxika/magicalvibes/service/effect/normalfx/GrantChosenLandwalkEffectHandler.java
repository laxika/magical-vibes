package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantChosenLandwalkEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenLandwalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantChosenLandwalkEffect) effect;
        // A self-grant still makes its choice when the creature has left (the ability resolves; the
        // grant then has nothing to affect). A targeted grant whose target is gone does not resolve.
        UUID recipientId;
        if (e.scope() == GrantScope.SELF) {
            recipientId = subjectResolutionService.permanentIds(gameData, entry, effect, GrantScope.SELF.subject())
                    .stream().findFirst().orElse(null);
        } else {
            Permanent recipient = subjectResolutionService.permanent(gameData, entry, effect,
                    GrantScope.TARGET.subject());
            if (recipient == null) {
                return;
            }
            recipientId = recipient.getId();
        }

        playerInputService.beginBasicLandwalkTypeChoice(gameData, entry.getControllerId(), recipientId);
    }
}
