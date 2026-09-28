package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TeachCastCopyEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeachCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TeachCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TeachCastCopyEffect teach = (TeachCastCopyEffect) effect;
        ExiledCardEntry taught = gameData.findExiledCard(teach.taughtCardId());
        if (taught == null) {
            return;
        }

        Card copy = copySupport.createCopyCard(taught.card());
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(teach),
                "Cast the copy of " + copy.getName() + " for its teach cost?",
                copy.getId()));
    }
}
