package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerAttachmentOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenPerAttachmentOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenPerAttachmentOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenPerAttachmentOnSourceEffect) effect;
        
                UUID sourcePermanentId = entry.getSourcePermanentId();
                if (sourcePermanentId == null) {
                    log.warn("Game {} - CreateTokenPerAttachmentOnSource requires sourcePermanentId", gameData.id);
                    return;
                }

                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source == null) {
                    log.info("Game {} - Source permanent no longer on battlefield, skipping token creation", gameData.id);
                    return;
                }

                // Count Auras and/or Equipment attached to the source permanent
                int attachmentCount = 0;
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (!p.isAttached() || !p.getAttachedTo().equals(sourcePermanentId)) continue;
                        boolean isAura = p.getCard().getSubtypes().contains(CardSubtype.AURA);
                        boolean isEquipment = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                        if ((e.countAuras() && isAura) || (e.countEquipment() && isEquipment)) {
                            attachmentCount++;
                        }
                    }
                }

                if (attachmentCount == 0) {
                    log.info("Game {} - No matching attachments on {}, no tokens created", gameData.id, entry.getCard().getName());
                    return;
                }

                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        CardType.CREATURE, attachmentCount, e.tokenName(), e.power(), e.toughness(),
                        e.color(), null, e.subtypes(), e.keywords(), e.additionalTypes(),
                        false, false, Map.of(), List.of(), false, e.exileAtEndStep(), false, 0, Set.of()
                );
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    
    }
}
