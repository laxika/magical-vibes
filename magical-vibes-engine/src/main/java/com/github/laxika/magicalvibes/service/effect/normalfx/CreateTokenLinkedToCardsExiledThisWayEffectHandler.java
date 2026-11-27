package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenLinkedToCardsExiledThisWayEffect;
import com.github.laxika.magicalvibes.model.effect.LinkCreatedTokensToExiledCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates the token of a {@link CreateTokenLinkedToCardsExiledThisWayEffect} and links the cards the resolution's
 * {@link StepKind#EXILE} step exiled to every token the creation made (a doubler's included), through a
 * {@link LinkCreatedTokensToExiledCardsEffect} step: each one's "the exiled card" is those cards, and a copy of one of
 * them returns nothing.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenLinkedToCardsExiledThisWayEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;
    private final LinkCreatedTokensToExiledCardsEffectHandler linkHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenLinkedToCardsExiledThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenLinkedToCardsExiledThisWayEffect) effect;
        List<UUID> exiledIds = List.copyOf(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds());
        int createdFrom = entry.getCreatedPermanentIds().size();
        List<UUID> tokenIds = permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.token(),
                entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(tokenIds);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability creates a token."));
        // The creation may stop for its replacements' order (CR 616.1): the tokens are linked by the next step, once
        // every token it made is known.
        LinkCreatedTokensToExiledCardsEffect link = new LinkCreatedTokensToExiledCardsEffect(createdFrom, exiledIds);
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(link));
        } else {
            linkHandler.resolve(gameData, entry, link);
        }
    }
}
