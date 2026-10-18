package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MadnessMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMadnessCostEffect;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the madness triggered ability: offer to cast the discarded card from exile for its
 * madness cost (CR 702.34b). No-ops if the card has left exile.
 */
@Slf4j
@Component
public class MadnessMayCastEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MadnessMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();
        MadnessMayCastEffect madnessEffect = (MadnessMayCastEffect) effect;

        if (gameData.findExiledCard(card.getId()) == null) {
            log.info("Game {} - madness may-cast skipped; {} no longer in exile", gameData.id, card.getName());
            return;
        }

        String cost = madnessEffect.madnessCost();
        if (cost == null) {
            cost = card.getCastingOption(MadnessCast.class)
                    .map(MadnessCast::manaCostString)
                    .orElse(null);
        }
        if (cost == null) {
            log.warn("Game {} - {} has no madness cost", gameData.id, card.getName());
            return;
        }

        int madnessLifeCost = card.getCastingOption(MadnessCast.class)
                .flatMap(madness -> madness.getCost(LifeCastingCost.class))
                .map(LifeCastingCost::amount)
                .orElse(0);
        String costText = madnessLifeCost > 0 ? cost + " and " + madnessLifeCost + " life" : cost;

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                card,
                controllerId,
                List.of(new MayCastForMadnessCostEffect()),
                "Cast " + card.getName() + " for its madness cost (" + costText + ")?",
                null,
                cost,
                null,
                null,
                0,
                madnessLifeCost,
                null, null, null, null, null, null, 0, null, null, null, null
        ));
        log.info("Game {} - offering madness cast of {} for {}", gameData.id, card.getName(), cost);
    }
}
