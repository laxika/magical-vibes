package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetLandWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnimateTargetLandWhileSourceOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateTargetLandWhileSourceOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimateTargetLandWhileSourceOnBattlefieldEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Per ruling: if the source creature left the battlefield before this ETB resolves,
        // nothing happens to the targeted land.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability has no effect (it is no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} ETB has no effect, source left battlefield", gameData.id, entry.getCard().getName());
            return;
        }

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(e.power());
        target.setPermanentAnimatedToughness(e.toughness());

        for (CardSubtype subtype : e.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        if (e.color() != null) {
            target.getGrantedColors().add(e.color());
        }

        gameData.sourceLinkedAnimations.put(target.getId(), sourcePermanentId);

        String logEntry = target.getCard().getName() + " becomes a " + e.power() + "/" + e.toughness()
                + " green Treefolk creature. It's still a land.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature while {} is on the battlefield",
                gameData.id, target.getCard().getName(), e.power(), e.toughness(),
                entry.getCard().getName());
    }
}
