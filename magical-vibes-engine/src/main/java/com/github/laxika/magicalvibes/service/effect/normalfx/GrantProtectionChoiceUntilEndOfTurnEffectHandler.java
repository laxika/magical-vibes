package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantProtectionChoiceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionChoiceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantProtectionChoiceUntilEndOfTurnEffect) effect;

        // Multi-target spells (Prismatic Boon's "X target creatures") carry their targets in the
        // flat list; single-target spells and abilities carry theirs on targetId.
        List<UUID> targetIds = resolveRecipientIds(entry, e);

        List<Permanent> targets = targetIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .toList();
        if (targets.isEmpty()) {
            return;
        }

        UUID choosingPlayerId = e.targetControllerChooses()
                ? gameQueryService.findPermanentController(gameData, targets.getFirst().getId())
                : entry.getControllerId();
        if (choosingPlayerId == null) {
            choosingPlayerId = entry.getControllerId();
        }

        playerInputService.beginProtectionColorChoice(gameData, choosingPlayerId,
                targets.stream().map(Permanent::getId).toList(), e.includeArtifacts());
    }

    /**
     * Self-scoped abilities ("this creature gains protection from the color of your choice") have no
     * target and resolve against the source permanent; targeted ones use the flat list (Prismatic
     * Boon's "X target creatures") or the single {@code targetId}.
     */
    private List<UUID> resolveRecipientIds(StackEntry entry, GrantProtectionChoiceUntilEndOfTurnEffect e) {
        if (e.scope() == GrantScope.SELF) {
            UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            return selfId == null ? List.of() : List.of(selfId);
        }
        return entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId()))
                : entry.getTargetIds();
    }
}
