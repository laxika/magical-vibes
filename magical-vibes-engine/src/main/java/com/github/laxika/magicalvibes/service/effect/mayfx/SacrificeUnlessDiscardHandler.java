package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Sacrifice-unless-discard — "unless you discard a [type] card, sacrifice [source]".
 * A decline, or an accepted answer that finds nothing left to pay with, changes nothing before it
 * sacrifices the source, and a payable acceptance only opens its choice (a random discard closes the
 * window first), so a creature with competing death replacements is asked about before it leaves
 * (CR 616.1) and the same answer is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class SacrificeUnlessDiscardHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessDiscardCardTypeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, accepted,
                () -> mayPenaltyChoiceHandlerService.handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability));
    }
}
