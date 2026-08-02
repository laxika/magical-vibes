package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CipherCastCopyEffect;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CipherCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CipherCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CipherCastCopyEffect cipher = (CipherCastCopyEffect) effect;
        ExiledCardEntry encoded = gameData.findExiledCard(cipher.encodedCardId());
        if (encoded == null) {
            return;
        }

        Card copy = copySupport.createCopyCard(encoded.card());
        gameData.addToExile(encoded.ownerId(), copy);
        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, entry.getControllerId(), List.of(copy.getId()));
    }
}
