package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromExileOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutTargetCardFromExileOnBottomOfOwnersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardFromExileOnBottomOfOwnersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCardFromExileOnBottomOfOwnersLibraryEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        UUID targetId = targetIds.stream().findFirst().orElse(null);
        if (targetId == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (no valid exile target)."));
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(targetId);
        Card targetCard = exiled == null ? null : exiled.card();
        String filterLabel = CardPredicateUtils.describeFilter(e.filter());
        if (exiled == null || exiled.faceDown()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target " + filterLabel
                            + " is no longer a face-up card in exile)."));
            return;
        }
        if (e.notOwnedOnly() && entry.getControllerId().equals(exiled.ownerId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target card is owned by the ability controller)."));
            return;
        }
        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is not a " + filterLabel + ")."));
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in exile)."));
            return;
        }

        gameData.playerDecks.get(exiled.ownerId()).addLast(targetCard);
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " puts ", targetCard,
                        " on the bottom of its owner's library."));
    }
}
