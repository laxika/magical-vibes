package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The "you may reveal the first card you draw each turn as you draw it" choice of a {@link RevealDrawnCardsEffect}
 * (God-Eternal Kefnet, Inquisitor Eisenhorn), asked during the draw (CR 121.9) with the drawn card as the question's
 * card: accepting reveals it, which triggers the source's "whenever you reveal a [card] this way" abilities; then the
 * paused draw continues.
 */
@Component
public class RevealDrawnCardsHandler implements MayEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    public RevealDrawnCardsHandler(@Lazy DrawService drawService, GameQueryService gameQueryService,
                                   InputCompletionService inputCompletionService) {
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            Card drawn = gameData.playerHands.getOrDefault(player.getId(), List.of()).stream()
                    .filter(card -> card.getId().equals(ability.targetCardId()))
                    .findFirst().orElse(null);
            if (source != null && drawn != null) {
                drawService.revealDrawnCard(gameData, source, player.getId(), drawn);
            }
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
