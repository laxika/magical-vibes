package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedTokensToHostsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link AttachCreatedTokensToHostsEffect}: the {@code n} Aura tokens the creation made are split over the
 * {@code h} hosts in order, token {@code i} going to host {@code i * h / n}, so each host gets as many as a doubler
 * made of its token. A token that can't enchant its host (it left, or gained protection) is left unattached.
 */
@Component
@RequiredArgsConstructor
public class AttachCreatedTokensToHostsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final DurationClock durationClock;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachCreatedTokensToHostsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attach = (AttachCreatedTokensToHostsEffect) effect;
        List<UUID> hostIds = attach.hostIds();
        List<UUID> created = entry.getCreatedPermanentIds();
        recordCreation(gameData, entry, attach.createdFrom());
        if (hostIds.isEmpty() || attach.createdFrom() >= created.size()) {
            return;
        }
        List<Permanent> auras = created.subList(attach.createdFrom(), created.size()).stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null && permanent.getCard().isAura()
                        && permanent.getAttachedTo() == null)
                .toList();
        for (int i = 0; i < auras.size(); i++) {
            Permanent aura = auras.get(i);
            Permanent host = gameQueryService.findPermanentById(gameData,
                    hostIds.get((int) ((long) i * hostIds.size() / auras.size())));
            UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
            if (host == null || auraControllerId == null
                    || !auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, host)) {
                continue;
            }
            durationClock.sourceUnattached(gameData, aura.getId());
            aura.setAttachedTo(host.getId());
            aura.setTimestamp(gameData.nextTimestamp());
            gameData.rulesEvents.occur(new AttachmentEvent.AuraAttached(aura, host.getId()));
        }
    }

    /**
     * The creation's CREATE_TOKEN result, the tokens it made from {@code createdFrom} on and their controllers, recorded
     * now that the creation has finished ("when you do", Curse of the Werefox): the creating step stopped for its
     * replacements' order before it knew them.
     */
    private void recordCreation(GameData gameData, StackEntry entry, int createdFrom) {
        List<UUID> all = entry.getCreatedPermanentIds();
        List<UUID> created = List.copyOf(all.subList(Math.min(createdFrom, all.size()), all.size()));
        List<UUID> controllers = new ArrayList<>();
        for (UUID tokenId : created) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, tokenId);
            if (controllerId != null) {
                controllers.add(controllerId);
            }
        }
        entry.recordStep(StepKind.CREATE_TOKEN, StepOutcome.ofObjects(created, controllers));
    }
}
