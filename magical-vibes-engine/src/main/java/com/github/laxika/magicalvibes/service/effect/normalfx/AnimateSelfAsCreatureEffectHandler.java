package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnimateSelfAsCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateSelfAsCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }

        int power = self.getCard().getPower() != null ? self.getCard().getPower() : 0;
        int toughness = self.getCard().getToughness() != null ? self.getCard().getToughness() : 0;
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(power);
        self.setAnimatedToughness(toughness);
        self.getGrantedCardTypes().add(CardType.CREATURE);

        String logEntry = self.getCard().getName() + " becomes a " + power + "/" + toughness
                + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} artifact creature (crewed)",
                gameData.id, self.getCard().getName(), power, toughness);
    }
}
