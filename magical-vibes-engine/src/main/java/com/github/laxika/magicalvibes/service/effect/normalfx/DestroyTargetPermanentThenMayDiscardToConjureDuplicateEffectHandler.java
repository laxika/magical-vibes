package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenMayDiscardToConjureDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Vona de Iedo's destruction and optional duplicate conjure. */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenMayDiscardToConjureDuplicateEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenMayDiscardToConjureDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Card targetCard = target.getCard();
        entry.rememberLastKnownPermanentCard(target.getId(), targetCard);
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);

        if (gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty()) {
            return;
        }

        MayEffect mayDiscard = new MayEffect(
                new DiscardCardThenEffect(null,
                        new ConjureDuplicateOfCardIntoHandEffect(targetCard), "a card"),
                "discard a card to conjure a duplicate of that permanent?");
        gameData.queueMayAbility(entry.getCard(), entry.getControllerId(), mayDiscard);
    }
}
