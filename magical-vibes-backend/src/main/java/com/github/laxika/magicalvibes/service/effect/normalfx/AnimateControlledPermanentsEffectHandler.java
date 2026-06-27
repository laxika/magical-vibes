package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnimateControlledPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateControlledPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimateControlledPermanentsEffect) effect;
        var battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                permanent.setAnimatedUntilEndOfTurn(true);
                permanent.setAnimatedPower(e.power());
                permanent.setAnimatedToughness(e.toughness());
                permanent.getGrantedCardTypes().add(CardType.CREATURE);

                // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
                if (permanent.isAttached() && permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    permanent.setAttachedTo(null);
                    String unattachLog = permanent.getCard().getName() + " becomes unattached.";
                    gameBroadcastService.logAndBroadcast(gameData, unattachLog);
                }
                count++;
            }
        }

        String logEntry = count + " artifact(s) become " + e.power() + "/" + e.toughness() + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} artifacts animated as {}/{} creatures until end of turn",
                gameData.id, count, e.power(), e.toughness());
    }
}
