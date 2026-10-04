package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayBlockCostsEffect;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Lets the defending player authorize costs for blocks proposed by another player. */
@Component
@RequiredArgsConstructor
public class BlockCostPaymentHandler implements MayEffectHandlerBean {
    private final ObjectProvider<CombatBlockService> combatBlockService;
    private final ObjectProvider<TurnProgressionService> turnProgressionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayBlockCostsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var payment = (PayBlockCostsEffect) ability.effects().getFirst();
        var result = combatBlockService.getObject().completeBlockCostPayment(gameData, payment, accepted);
        turnProgressionService.getObject().handleCombatResult(result, gameData);
    }
}
