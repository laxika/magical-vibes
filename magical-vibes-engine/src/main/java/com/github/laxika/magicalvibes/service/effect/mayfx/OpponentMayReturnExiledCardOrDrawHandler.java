package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Opponent may return the exiled card to your hand, or you draw N (e.g. Distant Memories). The
 * opponent is the decision maker.
 */
@Component
@RequiredArgsConstructor
public class OpponentMayReturnExiledCardOrDrawHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentMayReturnExiledCardOrDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        OpponentMayReturnExiledCardOrDrawEffect effect = ability.effects().stream()
                .filter(e -> e instanceof OpponentMayReturnExiledCardOrDrawEffect)
                .map(e -> (OpponentMayReturnExiledCardOrDrawEffect) e)
                .findFirst().orElse(null);
        mayPenaltyChoiceHandlerService.handleOpponentExileChoice(gameData, player, accepted, ability, effect);
    }
}
