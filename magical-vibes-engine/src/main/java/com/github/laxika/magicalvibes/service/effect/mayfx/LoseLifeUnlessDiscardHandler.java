package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Lose-life-unless-discard — "unless you discard a card, you lose N life". A declined answer loses the life before
 * anything else changes (an accepted one only starts the discard choice), so a Lich's Duel Mastery that loss would
 * sacrifice is asked about before any life is lost (CR 616.1) and the answer is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeUnlessDiscardHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessDiscardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, accepted,
                () -> mayPenaltyChoiceHandlerService.handleLoseLifeUnlessDiscardChoice(gameData, player, accepted, ability));
    }
}
