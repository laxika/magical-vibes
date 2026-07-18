package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetCreatureForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetCreatureForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetIds() == null || entry.getTargetIds().size() < 2) {
            return;
        }

        UUID tokenControllerId = entry.getTargetIds().getFirst();
        UUID creatureTargetId = entry.getTargetIds().get(1);
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, creatureTargetId);
        if (targetPermanent == null) {
            log.info("Game {} - Target creature no longer on battlefield, no token created", gameData.id);
            return;
        }

        Card sourceCard = targetPermanent.getCard();
        Card tokenCard = new Card();
        tokenCard.setName(sourceCard.getName());
        tokenCard.setType(sourceCard.getType());
        tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
        tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
        tokenCard.setToken(true);
        tokenCard.setColor(sourceCard.getColor());
        tokenCard.setSupertypes(sourceCard.getSupertypes());
        tokenCard.setPower(sourceCard.getPower());
        tokenCard.setToughness(sourceCard.getToughness());
        tokenCard.setCardText(sourceCard.getCardText());
        tokenCard.setSetCode(sourceCard.getSetCode());
        tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());
        if (sourceCard.getSubtypes() != null) {
            tokenCard.setSubtypes(new ArrayList<>(sourceCard.getSubtypes()));
        }
        if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
            tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
        }

        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : sourceCard.getEffectRegistrations(slot)) {
                tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (var ability : sourceCard.getActivatedAbilities()) {
            tokenCard.addActivatedAbility(ability);
        }
        tokenCard.copyTargetingFrom(sourceCard);

        Permanent tokenPermanent = new Permanent(tokenCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, tokenControllerId, tokenPermanent);

        String controllerName = gameData.playerIdToName.get(tokenControllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(controllerName + " creates a token copy of " , sourceCard, "."));
        log.info("Game {} - {} creates token copy of {} for {}", gameData.id, controllerName,
                sourceCard.getName(), tokenControllerId);

        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, tokenControllerId, tokenCard, null, false);
    }
}
