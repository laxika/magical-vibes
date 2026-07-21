package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Returns target spell from the stack to its owner's hand. Not a counter — does not check
 * uncounterable and does not go through Guile's counter replacement.
 */
@Component
@RequiredArgsConstructor
public class ReturnTargetSpellToHandEffectHandler implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetSpellToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }
        bounceSupport.returnSpellToOwnerHand(gameData, entry, targetCardId);
    }
}
