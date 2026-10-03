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
                && !gameQueryService.hasEffectiveSubtype(gameData, source, e.requiredSubtype())) {
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
            var subtypes = new java.util.LinkedHashSet<>(gameQueryService.effectiveCreatureSubtypes(gameData, source));
            subtypes.remove(e.replacedSubtype());
            subtypes.add(e.addedSubtype());
            boolean overriding = true;
            for (CardSubtype subtype : subtypes) {
                addSubtypeEffect(gameData, entry, source, subtype, overriding);
                overriding = false;
            }
        } else {
            if (!source.getGrantedSubtypes().contains(e.addedSubtype())) {
                source.getGrantedSubtypes().add(e.addedSubtype());
            }
            addSubtypeEffect(gameData, entry, source, e.addedSubtype(), false);
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
    private void addSubtypeEffect(GameData gameData, StackEntry entry, Permanent source,
                                  CardSubtype subtype, boolean overriding) {
        gameData.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                java.util.UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect(
                        subtype, com.github.laxika.magicalvibes.model.effect.GrantScope.TARGET, overriding),
                source.getId(), null, null,
                com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, 0));
    }

}
