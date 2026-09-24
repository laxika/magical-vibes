package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllCommandersAndPutThemOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Tevesh Szat's commander-control and command-zone effect. */
@Component
@RequiredArgsConstructor
public class GainControlOfAllCommandersAndPutThemOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAllCommandersAndPutThemOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZoneCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> cards = gameData.playerCommandZones.get(playerId);
            if (cards != null) {
                commandZoneCards.addAll(cards);
            }
        }

        Set<UUID> commandZoneCardIds = commandZoneCards.stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
        List<Permanent> commandersOnBattlefield = new ArrayList<>();
        Set<UUID> battlefieldCardIds = new HashSet<>();
        gameData.forEachPermanent((currentControllerId, permanent) -> {
            Card card = permanent.getCard();
            Card originalCard = permanent.getOriginalCard();
            boolean identifiedByCommandZone = commandZoneCardIds.contains(card.getId())
                    || commandZoneCardIds.contains(originalCard.getId());
            if (identifiedByCommandZone) {
                permanent.setCommander(true);
            }
            if (permanent.isCommander()) {
                if (!currentControllerId.equals(controllerId)) {
                    commandersOnBattlefield.add(permanent);
                }
                battlefieldCardIds.add(card.getId());
                battlefieldCardIds.add(originalCard.getId());
            }
        });

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (Permanent permanent : commandersOnBattlefield) {
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null,
                    entry.getCard().getName());
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> cards = gameData.playerCommandZones.get(playerId);
            if (cards == null || cards.isEmpty()) {
                continue;
            }
            List<Card> toMove = new ArrayList<>(cards);
            cards.clear();
            for (Card card : toMove) {
                if (battlefieldCardIds.contains(card.getId())) {
                    continue;
                }
                Permanent permanent = new Permanent(card, Zone.COMMAND);
                permanent.setCommander(true);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            }
        }
    }
}
