package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class BecomeCreatureTypeWithBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCreatureTypeWithBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BecomeCreatureTypeWithBasePowerToughnessEffect) effect;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        // Intervening "if": e.g. "If this creature is a Spirit, ...". Granted subtypes count.
        if (e.requiredSubtype() != null
                && !source.getCard().getSubtypes().contains(e.requiredSubtype())
                && !source.getGrantedSubtypes().contains(e.requiredSubtype())) {
            return;
        }

        if (e.replacesGrantedSubtypes()) {
            source.getGrantedSubtypes().clear();
            source.setProtectionFromOpponentsPermanently(false);
            source.getProtectionFromPlayerIdsPermanently().clear();
        }

        if (e.power() != null) {
            source.setBasePowerOverriddenPermanently(true);
            source.setPermanentBasePowerOverride(e.power());
            source.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
        }
        if (e.toughness() != null) {
            source.setBaseToughnessOverriddenPermanently(true);
            source.setPermanentBaseToughnessOverride(e.toughness());
            source.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
        }

        if (e.replacedSubtype() != null) {
            Card copy = source.getCard().createRuntimeCopy();
            ArrayList<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
            subtypes.removeIf(subtype -> subtype == e.replacedSubtype());
            if (!subtypes.contains(e.addedSubtype())) {
                subtypes.add(e.addedSubtype());
            }
            copy.setSubtypes(subtypes);
            copy.freeze();
            source.setCard(copy);
        } else if (!source.getGrantedSubtypes().contains(e.addedSubtype())) {
            source.getGrantedSubtypes().add(e.addedSubtype());
        }

        if (e.grantsProtectionFromOpponents()) {
            source.setProtectionFromOpponentsPermanently(true);
            source.getProtectionFromPlayerIdsPermanently().clear();
            for (var playerId : gameData.playerIds) {
                if (!playerId.equals(entry.getControllerId())) {
                    source.getProtectionFromPlayerIdsPermanently().add(playerId);
                }
            }
        }

        String stats = e.power() != null && e.toughness() != null
                ? " with base power and toughness " + e.power() + "/" + e.toughness()
                : "";
        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" becomes a " + e.addedSubtype().getDisplayName() + stats + ".").build());
    }
}
