package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfReturnedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfReturnedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfReturnedPermanentEffect copyEffect =
                (CreateTokenCopyOfReturnedPermanentEffect) effect;
        UUID returnedCardId = returnedCardId(entry);
        if (returnedCardId == null) {
            return;
        }

        Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
        if (returnedPermanent == null || !matchesCondition(gameData, returnedPermanent, copyEffect.condition())) {
            return;
        }

        UUID originalTargetId = entry.getTargetId();
        entry.setTargetId(returnedPermanent.getId());
        try {
            tokenCopyHandler.resolve(gameData, entry, new CreateTokenCopyOfTargetPermanentEffect(
                    List.of(), Set.of(), copyEffect.powerOverride(), copyEffect.toughnessOverride(), Map.of()));
        } finally {
            entry.setTargetId(originalTargetId);
        }
    }

    private boolean matchesCondition(GameData gameData, Permanent permanent, PermanentPredicate condition) {
        return condition == null || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, condition);
    }

    private UUID returnedCardId(StackEntry entry) {
        if (entry.getTargetId() != null) {
            return entry.getTargetId();
        }
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            return entry.getTargetIds().getFirst();
        }
        if (entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            return entry.getTargetCardIds().getFirst();
        }
        return null;
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
