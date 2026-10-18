package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfAttachedAurasAndEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Copies the source's attached Auras and Equipment onto the first token created by the resolution. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateTokenCopiesOfAttachedAurasAndEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfAttachedAurasAndEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, entry.getCreatedPermanentIds().getFirst());
        if (host == null) {
            return;
        }

        List<Permanent> attached = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isAttached()
                    && source.getId().equals(permanent.getAttachedTo())
                    && (permanent.getCard().isAura()
                    || permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))) {
                attached.add(permanent);
            }
        });
        if (attached.isEmpty()) {
            return;
        }

        List<UUID> copiedIds = tokenCopySupport.createTokenCopiesAttachedTo(
                gameData,
                entry,
                attached.stream().map(Permanent::getCard).toList(),
                source,
                entry.getControllerId(),
                new CreateTokenCopyOfTargetPermanentEffect(),
                host);

        for (UUID copiedId : copiedIds) {
            Permanent copy = gameQueryService.findPermanentById(gameData, copiedId);
            if (copy == null) {
                continue;
            }
            if (copy.getCard().isAura()) {
                if (host.getId().equals(copy.getAttachedTo())) {
                    logAttachment(gameData, copy, host);
                }
            } else if (copy.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                if (equipSupport.attachEquipment(gameData, copy, host)) {
                    logAttachment(gameData, copy, host);
                }
            }
        }
    }

    private void logAttachment(GameData gameData, Permanent attachment, Permanent host) {
        gameLogService.append(gameData,
                GameLog.cardTextCard(attachment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                attachment.getCard().getName(), host.getCard().getName());
    }
}
