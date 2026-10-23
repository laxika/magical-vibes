package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Sacrifice-unless-return — "unless you return a [type] to its owner's hand, sacrifice [source]".
 * A decline changes nothing before it sacrifices the source, so a creature with competing death
 * replacements is asked about before it leaves (CR 616.1) and the decline is completed again with
 * that answer.
 */
@Component
@RequiredArgsConstructor
public class SacrificeUnlessReturnPermanentHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessReturnPermanentTypeToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            mayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnPermanentChoice(gameData, player, true, ability);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability,
                () -> mayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnPermanentChoice(gameData, player, false, ability));
    }
}
