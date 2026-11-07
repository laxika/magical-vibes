package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BuddyListGrowOrWriteEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves {@link BuddyListGrowOrWriteEffect} against the stack entry's triggering creature. */
@Component
@RequiredArgsConstructor
public class BuddyListGrowOrWriteEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BuddyListGrowOrWriteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEventValue() == 1) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        Set<CardSubtype> buddies = gameData.getBuddyList(entry.getControllerId());
        List<CardSubtype> enteringTypes = triggeringCreatureTypes(gameData, entry);
        boolean matchingType = enteringTypes.stream().anyMatch(buddies::contains);
        if (matchingType) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, source, 1);
            }
            return;
        }

        List<CardSubtype> choices = enteringTypes.stream()
                .filter(subtype -> !buddies.contains(subtype))
                .distinct()
                .toList();
        if (choices.isEmpty()) {
            return;
        }

        entry.setEventValue(1);
        if (choices.size() == 1) {
            gameData.addBuddy(entry.getControllerId(), choices.getFirst());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginBuddyListChoice(gameData, entry.getControllerId(), choices);
    }

    /**
     * The creature types of the entry's triggering permanent: as it is on the battlefield (every creature type for
     * changeling), else as it last existed there.
     */
    private List<CardSubtype> triggeringCreatureTypes(GameData gameData, StackEntry entry) {
        UUID permanentId = entry.getTriggeringPermanentId();
        if (permanentId == null) {
            return List.of();
        }
        Permanent live = gameQueryService.findPermanentById(gameData, permanentId);
        Set<CardSubtype> types = new LinkedHashSet<>();
        if (live != null) {
            types.addAll(gameQueryService.effectiveCreatureSubtypes(gameData, live));
            if (gameQueryService.hasKeyword(gameData, live, Keyword.CHANGELING)) {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (gameQueryService.isCreatureSubtype(subtype)) {
                        types.add(subtype);
                    }
                }
            }
            return List.copyOf(types);
        }
        Card lastKnown = entry.getTriggeringPermanentLastKnownCard();
        if (lastKnown == null && entry.getAttachedPermanentSnapshot() != null
                && permanentId.equals(entry.getAttachedPermanentSnapshot().getId())) {
            lastKnown = entry.getAttachedPermanentSnapshot().getCard();
        }
        if (lastKnown != null) {
            for (CardSubtype subtype : lastKnown.getSubtypes()) {
                if (gameQueryService.isCreatureSubtype(subtype)) {
                    types.add(subtype);
                }
            }
        }
        return List.copyOf(types);
    }
}
