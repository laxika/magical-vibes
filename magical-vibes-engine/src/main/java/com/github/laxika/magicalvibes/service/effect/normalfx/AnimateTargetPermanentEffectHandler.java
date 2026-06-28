package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnimateTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimateTargetPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
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

        target.getGrantedKeywords().addAll(e.grantedKeywords());

        // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
        if (target.isAttached() && target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            target.setAttachedTo(null);
            String unattachLog = target.getCard().getName() + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached (equipment became creature)", gameData.id, target.getCard().getName());
        }

        String logEntry = target.getCard().getName() + " becomes a " + e.power() + "/" + e.toughness() + " creature.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature permanently", gameData.id, target.getCard().getName(), e.power(), e.toughness());
    }
}
