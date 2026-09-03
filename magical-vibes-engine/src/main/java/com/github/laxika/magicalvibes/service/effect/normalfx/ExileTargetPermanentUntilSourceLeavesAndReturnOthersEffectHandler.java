package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ExileTargetPermanentUntilSourceLeavesEffectHandler exileHandler;
    private final ReturnAllCardsExiledWithSourceEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card targetCard = target.getOriginalCard();
        exileHandler.resolve(gameData, entry,
                new ExileTargetPermanentUntilSourceLeavesEffect(false,
                        exileEffect.targetPredicate()));

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(targetCard.getId());
        if (exiledEntry == null || !sourcePermanentId.equals(exiledEntry.sourcePermanentId())) {
            return;
        }

        returnHandler.returnAllCardsExiledWithSourceExcept(gameData, entry, targetCard.getId());
    }
}
