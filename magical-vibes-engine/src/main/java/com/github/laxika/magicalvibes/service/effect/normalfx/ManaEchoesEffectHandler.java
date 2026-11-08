package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaEchoesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManaEchoesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManaEchoesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "It" is the stack entry's triggering permanent: as it is now, else as it last existed on the battlefield.
        UUID enteringId = entry.getTriggeringPermanentId();
        Permanent enteringPermanent = enteringId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringId);
        Card lastKnownEntering = enteringPermanent != null ? null
                : entry.getTriggeringPermanentLastKnownCard() != null ? entry.getTriggeringPermanentLastKnownCard()
                : enteringId != null && entry.lastKnownPermanentCard(enteringId) != null
                ? entry.lastKnownPermanentCard(enteringId)
                // The creature as it entered, when it left before any last-known information was recorded.
                : entry.getTriggeringCardSnapshot();
        Set<CardSubtype> enteringTypes;
        if (enteringPermanent != null) {
            enteringTypes = creatureTypes(gameData, enteringPermanent);
        } else if (lastKnownEntering != null) {
            enteringTypes = new HashSet<>();
            for (CardSubtype subtype : lastKnownEntering.getSubtypes()) {
                if (gameQueryService.isCreatureSubtype(subtype)) {
                    enteringTypes.add(subtype);
                }
            }
            if (lastKnownEntering.getKeywords().contains(Keyword.CHANGELING)) {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (gameQueryService.isCreatureSubtype(subtype)) {
                        enteringTypes.add(subtype);
                    }
                }
            }
        } else {
            enteringTypes = Set.of();
        }

        int matchingCreatures = 0;
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            Set<CardSubtype> creatureTypes = creatureTypes(gameData, permanent);
            if (creatureTypes.stream().anyMatch(enteringTypes::contains)) {
                matchingCreatures++;
            }
        }

        if (matchingCreatures == 0) {
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(entry.getControllerId());
        ManaProductionSupport.add(gameData, entry.getControllerId(), pool,
                ManaColor.COLORLESS, matchingCreatures);
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + matchingCreatures + " {C}."));
        log.info("Game {} - {} adds {} colorless mana from Mana Echoes",
                gameData.id, playerName, matchingCreatures);
    }

    private Set<CardSubtype> creatureTypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> types = new HashSet<>(
                gameQueryService.effectiveCreatureSubtypes(gameData, permanent));
        if (gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
            for (CardSubtype subtype : CardSubtype.values()) {
                if (gameQueryService.isCreatureSubtype(subtype)) {
                    types.add(subtype);
                }
            }
        }
        return types;
    }
}
