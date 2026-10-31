package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedZoneChangeTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a token using the normal token pipeline and records the source/token relationship needed
 * by Stangg-style paired leaves-the-battlefield abilities.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenAndLinkToSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAndLinkToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var linked = (CreateTokenAndLinkToSourceEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null && !linked.linkTokenToSource() && entry.getSourcePermanentSnapshot() != null) {
            source = new Permanent(entry.getSourcePermanentSnapshot());
        }

        CreateTokenEffect token = linked.token();
        if (source != null && linked.linkTokenToSource()) {
            List<Ability> tokenEffects = new ArrayList<>();
            if (token.tokenEffects() != null) {
                // The blueprint's own leaves-the-battlefield ability is replaced, as the slot map did.
                token.tokenEffects().stream()
                        .filter(ability -> LegacySlots.slotOf(ability) != EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
                        .forEach(tokenEffects::add);
            }
            tokenEffects.add(LegacySlots.triggered(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE_PARTNER)));
            token = token.withTokenEffects(tokenEffects);
        }

        int firstCreatedIndex = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, token);
        if (source == null || entry.getCreatedPermanentIds().size() == firstCreatedIndex) {
            return;
        }

        List<UUID> createdIds = new ArrayList<>(entry.getCreatedPermanentIds()
                .subList(firstCreatedIndex, entry.getCreatedPermanentIds().size()));
        gameData.sourceCreatedTokens
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .addAll(createdIds);
        if (linked.leavesBattlefieldEffect() != null) {
            Map<Zone, CardEffect> effectsByDestination = new EnumMap<>(Zone.class);
            for (Zone destination : List.of(Zone.GRAVEYARD, Zone.EXILE, Zone.HAND, Zone.LIBRARY, Zone.COMMAND)) {
                effectsByDestination.put(destination, linked.leavesBattlefieldEffect());
            }
            for (UUID createdId : createdIds) {
                gameData.queueDelayedAction(new DelayedZoneChangeTrigger(createdId, entry.getControllerId(),
                        entry.getCard(), effectsByDestination, source.getId()));
            }
        }
        if (linked.sourceLeavesBattlefieldEffect() != null
                && gameQueryService.findPermanentById(gameData, source.getId()) != null) {
            Map<Zone, CardEffect> effectsByDestination = new EnumMap<>(Zone.class);
            for (Zone destination : List.of(Zone.GRAVEYARD, Zone.EXILE, Zone.HAND, Zone.LIBRARY, Zone.COMMAND)) {
                effectsByDestination.put(destination, linked.sourceLeavesBattlefieldEffect());
            }
            gameData.queueDelayedAction(new DelayedZoneChangeTrigger(source.getId(), entry.getControllerId(),
                    entry.getCard(), effectsByDestination, source.getId()));
        }
        for (UUID createdId : createdIds) {
            Permanent tokenPermanent = gameQueryService.findPermanentById(gameData, createdId);
            if (tokenPermanent != null) {
                tokenPermanent.setChosenPermanentId(source.getId());
            }
        }
    }
}
