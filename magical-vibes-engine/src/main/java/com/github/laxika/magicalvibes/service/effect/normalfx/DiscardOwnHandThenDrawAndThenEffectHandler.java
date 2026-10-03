package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawAndThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscardOwnHandThenDrawAndThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardOwnHandThenDrawAndThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var discardEffect = (DiscardOwnHandThenDrawAndThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            CardEffect followUp = discardEffect.reflexiveFollowUp()
                    ? new com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect(discardEffect.thenEffect())
                    : discardEffect.thenEffect();
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(discardEffect.drawCount()),
                            followUp));
            return;
        }

        Permanent currentSource = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceSnapshot = currentSource == null
                ? entry.getSourcePermanentSnapshot() : new Permanent(currentSource);
        DiscardFollowUp followUp = DiscardFollowUp.thenEffect(entry.getCard(), discardEffect.thenEffect())
                .withRummageDrawCount(discardEffect.drawCount())
                .withSameResolutionContinuation(!discardEffect.reflexiveFollowUp())
                .withSourceContext(entry.getSourcePermanentId(), sourceSnapshot, hand.size());
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, hand.size(), followUp);
    }
}
