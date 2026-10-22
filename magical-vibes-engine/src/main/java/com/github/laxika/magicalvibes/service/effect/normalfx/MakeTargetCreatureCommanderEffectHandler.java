package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreatureCommanderEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the commander designation created by You're in Command. */
@Component
@RequiredArgsConstructor
public class MakeTargetCreatureCommanderEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCreatureCommanderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (!controllerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))
                || !controllerId.equals(gameData.defaultControllerOf(target.getId()))
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        // A melded permanent is represented by both of its physical cards; the back-face card is not a
        // real card, so the designation goes on the components (CR 903.3 makes it an attribute of cards).
        List<Card> designated = target.getMeldComponentCards().isEmpty()
                ? List.of(target.getOriginalCard()) : List.copyOf(target.getMeldComponentCards());
        gameData.makeCommander(controllerId, designated.getFirst());
        designated.stream().skip(1).forEach(card -> gameData.addCommander(controllerId, card));
    }
}
