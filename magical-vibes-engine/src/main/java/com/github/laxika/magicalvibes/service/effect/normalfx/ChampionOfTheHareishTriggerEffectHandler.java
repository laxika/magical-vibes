package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionOfTheHareishTriggerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Resolves the custom buddy-list trigger used by Champion of the Hareish. */
@Component
@RequiredArgsConstructor
public class ChampionOfTheHareishTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChampionOfTheHareishTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEventValue() == 1) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        ChampionOfTheHareishTriggerEffect trigger = (ChampionOfTheHareishTriggerEffect) effect;
        Set<CardSubtype> buddies = gameData.getBuddyList(entry.getControllerId());
        Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        List<CardSubtype> enteringTypes = entering == null ? trigger.enteringSubtypes()
                : gameQueryService.effectiveCreatureSubtypes(gameData, entering).stream().toList();
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
}
