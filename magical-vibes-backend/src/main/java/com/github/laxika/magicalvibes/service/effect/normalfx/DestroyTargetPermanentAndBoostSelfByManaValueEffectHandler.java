package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetPermanentAndBoostSelfByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndBoostSelfByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                int manaValue = target.getCard().getManaValue();

                // Attempt to destroy the artifact
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Boost self by mana value regardless of destruction result
                Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (self != null && manaValue > 0) {
                    self.setPowerModifier(self.getPowerModifier() + manaValue);

                    String boostLog = entry.getCard().getName() + " gets +" + manaValue + "/+0 until end of turn.";
                    gameBroadcastService.logAndBroadcast(gameData, boostLog);
                    log.info("Game {} - {} gets +{}/+0 from {}'s mana value", gameData.id, entry.getCard().getName(), manaValue, target.getCard().getName());
                }
    }
}
