package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileGraveyardCardWithConditionalBonusEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardCardWithConditionalBonusEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileGraveyardCardWithConditionalBonusEffect) effect;

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        if (graveyardOwnerId != null) {
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        }

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + targetCard.getName() + " from a graveyard."));

        boolean isCreatureCard = targetCard.hasType(CardType.CREATURE);
        if (isCreatureCard) {
            // Creature card exiled: gain life
            lifeSupport.applyGainLife(gameData, controllerId, e.creatureLifeGain(),
                    entry.getCard().getName(), entry.getCard(), entry.getEntryType());
        } else {
            // Noncreature card exiled: boost source permanent
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source != null) {
                    source.setPowerModifier(source.getPowerModifier() + e.noncreaturePowerBoost());
                    source.setToughnessModifier(source.getToughnessModifier() + e.noncreatureToughnessBoost());

                    String boostLog = source.getCard().getName() + " gets +"
                            + e.noncreaturePowerBoost() + "/+" + e.noncreatureToughnessBoost()
                            + " until end of turn.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(boostLog));
                    log.info("Game {} - {} gets +{}/+{}", gameData.id, source.getCard().getName(),
                            e.noncreaturePowerBoost(), e.noncreatureToughnessBoost());
                }
            }
        }
    }
}
