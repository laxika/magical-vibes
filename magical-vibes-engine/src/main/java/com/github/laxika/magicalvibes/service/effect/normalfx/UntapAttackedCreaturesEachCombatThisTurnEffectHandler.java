package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEachCombatThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "At the beginning of each combat this turn, untap all creatures that attacked this turn" (Full Throttle): a delayed
 * triggered ability with a stated duration, the spell's and controlled by its controller (CR 603.7b, 603.7e), that
 * triggers at every combat of the turn, whoever's it is.
 */
@Slf4j
@Component
public class UntapAttackedCreaturesEachCombatThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapAttackedCreaturesEachCombatThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.beginningOfCombat(ActivePlayer.ANY),
                        new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)),
                entry.getSourcePermanentId()));
        log.info("Game {} - {} will untap attacked creatures at the beginning of each combat this turn",
                gameData.id, entry.getCard().getName());
    }
}
