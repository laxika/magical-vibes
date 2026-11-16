package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.PlayersDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import java.util.List;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextMainPhaseEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterDrawCardsAtNextMainPhaseEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextMainPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Draw a card for each player who was dealt combat damage this turn" is counted as the ability resolves.
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.ANY)),
                        new TriggerCondition(Triggers.postcombatMainPhase(ActivePlayer.ANY))),
                        TriggerOptions.DEFAULT, List.of(new EffectRegistration(
                                new DrawCardEffect(new PlayersDealtCombatDamageThisTurn())))),
                entry.getSourcePermanentId()));
        log.info("Game {} - {} registers a delayed draw at their next main phase",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()));
    }
}
