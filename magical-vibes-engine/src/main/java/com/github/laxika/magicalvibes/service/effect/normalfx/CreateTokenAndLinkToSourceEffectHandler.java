package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
                        .filter(ability -> !(ability instanceof TriggeredAbility triggered
                                && triggered.hasTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS))))
                        .forEach(tokenEffects::add);
            }
            tokenEffects.add(new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
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
        // "When that token leaves the battlefield" and "when this creature leaves the battlefield": delayed triggered
        // abilities about the token or the source, with the source as their source (CR 603.7e) and no stated
        // duration (CR 603.7b).
        if (linked.leavesBattlefieldEffect() != null) {
            for (UUID createdId : createdIds) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                        entry.getControllerId(), entry.getCard(),
                        new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                                linked.leavesBattlefieldEffect()),
                        createdId, source.getId(), false));
            }
        }
        if (linked.sourceLeavesBattlefieldEffect() != null
                && gameQueryService.findPermanentById(gameData, source.getId()) != null) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                            linked.sourceLeavesBattlefieldEffect()),
                    source.getId(), source.getId(), false));
        }
        for (UUID createdId : createdIds) {
            Permanent tokenPermanent = gameQueryService.findPermanentById(gameData, createdId);
            if (tokenPermanent != null) {
                tokenPermanent.setChosenPermanentId(source.getId());
            }
        }
    }
}
