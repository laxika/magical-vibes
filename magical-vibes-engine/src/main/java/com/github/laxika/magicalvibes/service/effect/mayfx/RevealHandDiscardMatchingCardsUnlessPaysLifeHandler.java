package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandDiscardMatchingCardsUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Per-revealed-card pay-or-discard (Sirocco): "discard that card unless they pay N life".
 */
@Component
@RequiredArgsConstructor
public class RevealHandDiscardMatchingCardsUnlessPaysLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandDiscardMatchingCardsUnlessPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealHandDiscardMatchingCardsUnlessPaysLifeEffect effect = ability.effects().stream()
                .filter(RevealHandDiscardMatchingCardsUnlessPaysLifeEffect.class::isInstance)
                .map(RevealHandDiscardMatchingCardsUnlessPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayPenaltyChoiceHandlerService.handleRevealHandDiscardUnlessPaysLifeChoice(
                gameData, player, accepted, ability, effect);
    }
}
