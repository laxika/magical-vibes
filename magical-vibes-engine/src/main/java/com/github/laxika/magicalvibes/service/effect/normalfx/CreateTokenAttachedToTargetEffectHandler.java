package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenAttachedToTargetEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final DurationClock durationClock;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAttachedToTargetEffect) effect;
        if (e.oneOfTriggeringPermanents()) {
            resolveOneOfTriggeringPermanents(gameData, entry, e);
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(e);
        // Unbound effects may store their single target separately from the flat target list.
        // An empty bound group must stay empty even if another group still has a target.
        if (targetIds.isEmpty() && entry.targetsForBoundEffectGroup(e) == null && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        resolve(gameData, entry, e, targetIds);
    }

    /**
     * "Attached to one of them": the token is attached to one of the permanents whose event made the ability trigger
     * that still fit it, which its controller chooses as it resolves when there are several (not a target).
     */
    private void resolveOneOfTriggeringPermanents(GameData gameData, StackEntry entry,
                                                  CreateTokenAttachedToTargetEffect e) {
        List<UUID> candidates = triggeringCandidates(gameData, entry, e);
        if (candidates.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.CreateTokenAttachedToOneOfTriggeringPermanents(e));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                    "Choose one of the creatures that entered to attach the token to.");
            return;
        }
        candidates.forEach(candidateId -> createAttachedToken(gameData, entry, e, candidateId));
    }

    /** Attaches the token of the effect waiting in {@code context} to the chosen {@code chosenPermanentId}. */
    public void completeTriggeringPermanentChoice(
            GameData gameData, UUID chosenPermanentId,
            PermanentChoiceContext.CreateTokenAttachedToOneOfTriggeringPermanents context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }
        if (!triggeringCandidates(gameData, entry, context.effect()).contains(chosenPermanentId)) {
            throw new IllegalStateException("Choose one of the creatures that entered");
        }
        createAttachedToken(gameData, entry, context.effect(), chosenPermanentId);
    }

    private List<UUID> triggeringCandidates(GameData gameData, StackEntry entry,
                                            CreateTokenAttachedToTargetEffect e) {
        List<UUID> triggeringIds = !entry.getTriggeringPermanentIds().isEmpty() ? entry.getTriggeringPermanentIds()
                : entry.getTriggeringPermanentId() == null ? List.of() : List.of(entry.getTriggeringPermanentId());
        return triggeringIds.stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent != null && (e.targetFilter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                            e.targetFilter()));
                })
                .toList();
    }

    void resolve(GameData gameData, StackEntry entry, CreateTokenAttachedToTargetEffect e,
                 List<UUID> targetIds) {
        for (UUID targetId : targetIds) {
            createAttachedToken(gameData, entry, e, targetId);
        }
    }

    private void createAttachedToken(GameData gameData, StackEntry entry,
                                     CreateTokenAttachedToTargetEffect e, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetControllerId = target == null
                ? null : gameQueryService.findPermanentController(gameData, target.getId());
        boolean targetControllerMatches = switch (e.targetControllerRelation()) {
            case SELF -> entry.getControllerId().equals(targetControllerId);
            case OPPONENT -> targetControllerId != null && !entry.getControllerId().equals(targetControllerId);
            case ANY -> true;
        };
        if (target == null || !targetControllerMatches
                || e.targetFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.targetFilter())) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);

        Card preview = TokenCardFactory.create(e.token(), power, toughness, entry.getCard().getSetCode());
        Permanent previewPermanent = new Permanent(preview);
        if (!auraAttachmentService.canEnchant(gameData, preview, entry.getControllerId(), target)
                || gameQueryService.hasProtectionFromSource(gameData, target, previewPermanent)) {
            return;
        }

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), amount,
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        for (UUID createdId : createdIds) {
            Permanent role = gameQueryService.findPermanentById(gameData, createdId);
            if (role == null || !auraAttachmentService.canEnchant(
                    gameData, role.getCard(), entry.getControllerId(), target)) {
                continue;
            }
            durationClock.sourceUnattached(gameData, role.getId());
            role.setAttachedTo(target.getId());
            role.setTimestamp(gameData.nextTimestamp());
            gameData.rulesEvents.occur(new AttachmentEvent.AuraAttached(role, target.getId()));
        }
    }
}
