package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Play-from-library — e.g. Djinn of Wishes (play any card or exile it),
 * Descendants' Path (cast the matching creature or bottom it).
 */
@Component
public class RevealTopCardMayPlayFreeHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final DrawService drawService;
    private final InputCompletionService inputCompletionService;

    public RevealTopCardMayPlayFreeHandler(MayCastHandlerService mayCastHandlerService,
                                            @Lazy DrawService drawService,
                                            InputCompletionService inputCompletionService) {
        this.mayCastHandlerService = mayCastHandlerService;
        this.drawService = drawService;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPlayFreeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealTopCardMayPlayFreeEffect effect = ability.effects().stream()
                .filter(RevealTopCardMayPlayFreeEffect.class::isInstance)
                .map(RevealTopCardMayPlayFreeEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (!accepted && effect.drawIfDeclined()) {
            UUID libraryOwnerId = effect.libraryOwnerId() != null ? effect.libraryOwnerId() : player.getId();
            drawService.resolveDrawCard(gameData, libraryOwnerId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        mayCastHandlerService.handlePlayFromLibraryOrExileChoice(gameData, player, accepted, ability);
    }
}
