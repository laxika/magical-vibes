package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
    private final PredicateEvaluationService predicateEvaluationService;
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
        List<UUID> targetIds = resolveRecipientIds(gameData, entry, e);

        List<Permanent> targets = targetIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .toList();
        // With no qualifying permanents there is nothing the colour choice could apply to.
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
     * target and resolve against the source permanent; the mass form scans the controller's own
     * creatures on resolution; targeted ones use the flat list (Prismatic Boon's "X target
     * creatures") or the single {@code targetId}.
     */
    private List<UUID> resolveRecipientIds(GameData gameData, StackEntry entry,
                                           GrantProtectionChoiceUntilEndOfTurnEffect e) {
        if (e.scope() == GrantScope.SELF) {
            UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            return selfId == null ? List.of() : List.of(selfId);
        }
        if (e.scope() == GrantScope.OWN_CREATURES) {
            return ownMatchingCreatureIds(gameData, entry, e);
        }
        return entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId()))
                : entry.getTargetIds();
    }

    private List<UUID> ownMatchingCreatureIds(GameData gameData, StackEntry entry,
                                              GrantProtectionChoiceUntilEndOfTurnEffect e) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return List.of();
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext))
                .map(Permanent::getId)
                .toList();
    }
}
