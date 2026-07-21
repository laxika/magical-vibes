package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoulbondChoosePartnerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SoulbondSupport soulbondSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulbondChoosePartnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            // Fall back to card match when sourcePermanentId wasn't threaded (self-ETB may).
            source = findByCard(gameData, controllerId, entry);
        }
        if (source == null || !soulbondSupport.isUnpairedCreature(gameData, source)) {
            return;
        }

        List<UUID> validIds = soulbondSupport.collectUnpairedPartnerIds(gameData, source, controllerId);
        if (validIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SoulbondChoosePartner(source.getId(), controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose another unpaired creature to pair with.");
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.textCardText(playerName + " may pair ", entry.getCard(), " with another creature."));
        log.info("Game {} - {} choosing soulbond partner", gameData.id, entry.getCard().getName());
    }

    private Permanent findByCard(GameData gameData, UUID controllerId, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent p : battlefield) {
            if (p.getCard().getId().equals(entry.getCard().getId())) {
                return p;
            }
        }
        return null;
    }
}
