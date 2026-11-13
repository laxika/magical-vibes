package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.effect.ReturnedPermanentFightsTargetCreatureEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnedPermanentFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnedPermanentFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnedPermanentFightsTargetCreatureEffect fightEffect =
                (ReturnedPermanentFightsTargetCreatureEffect) effect;
        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            returnedCardId = entry.getTargetCardIds().getFirst();
        }
        if (returnedCardId == null) {
            return;
        }

        Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
        if (returnedPermanent == null) {
            return;
        }

        // The returned creature is the reflexive ability's source permanent: "it fights up to one target creature".
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry,
                List.of(new EnteringCreatureFightsTargetCreatureEffect(fightEffect.targetPredicate(), true)),
                entry.getCard().getName() + "'s reflexive ability", returnedPermanent.getId());
        record.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        triggeredAbilityStackService.putMade(gameData, record);
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
