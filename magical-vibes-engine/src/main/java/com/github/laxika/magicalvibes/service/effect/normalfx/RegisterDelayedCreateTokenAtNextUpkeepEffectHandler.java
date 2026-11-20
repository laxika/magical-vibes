package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreateTokenAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreateTokenAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedCreateTokenAtNextUpkeepEffect) effect;
        // The delayed ability creates the tokens as the card wrote them; "for each creature returned to your hand this
        // way" is counted now, from this resolution's step result, and the delayed ability carries it as its own number.
        CreateTokenEffect tokens = e.tokenEffect().amount() instanceof AmountThisWay thisWay
                ? e.tokenEffect().withAmount(amountEvaluationService.evaluate(gameData, thisWay,
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())))
                : e.tokenEffect();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.upkeep(e.anyPlayerNextUpkeep() ? ActivePlayer.ANY : ActivePlayer.YOU),
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null, tokens));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - {} registers delayed token creation at next upkeep",
                gameData.id, playerName);
    }
}
