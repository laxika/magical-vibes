package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAttachedToTargetEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        if (e.subject() instanceof PermanentSubject.Triggering) {
            if (resolveOneOfTriggeringPermanents(gameData, entry, e)) {
                recordCreated(gameData, entry, createdBefore);
            }
            return;
        }
        if (!(e.subject() instanceof PermanentSubject.Target)) {
            subjectResolutionService.permanents(gameData, entry, e, e.subject())
                    .forEach(host -> createAttachedToken(gameData, entry, e, host.getId()));
            recordCreated(gameData, entry, createdBefore);
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(e);
        // Unbound effects may store their single target separately from the flat target list.
        // An empty bound group must stay empty even if another group still has a target.
        if (targetIds.isEmpty() && entry.targetsForBoundEffectGroup(e) == null && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        resolve(gameData, entry, e, targetIds);
        recordCreated(gameData, entry, createdBefore);
    }

    /** The step's result: the tokens it created and their controllers ("when you do", Curse of the Werefox). */
    private void recordCreated(GameData gameData, StackEntry entry, int createdBefore) {
        List<UUID> created = List.copyOf(entry.getCreatedPermanentIds()
                .subList(createdBefore, entry.getCreatedPermanentIds().size()));
        List<UUID> controllers = new ArrayList<>();
        for (UUID tokenId : created) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, tokenId);
            if (controllerId != null) {
                controllers.add(controllerId);
            }
        }
        entry.recordStep(StepKind.CREATE_TOKEN, StepOutcome.ofObjects(created, controllers));
    }

    /**
     * "Attached to that creature" / "to one of them": the token is attached to the permanent whose event made the ability
     * trigger, or to one of them that still fits it, which its controller chooses as it resolves when there are several
     * (not a target).
     *
     * @return whether the step finished without asking
     */
    private boolean resolveOneOfTriggeringPermanents(GameData gameData, StackEntry entry,
                                                  CreateTokenAttachedToTargetEffect e) {
        List<UUID> candidates = triggeringCandidates(gameData, entry, e);
        if (candidates.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.CreateTokenAttachedToOneOfTriggeringPermanents(e));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                    "Choose one of the creatures that entered to attach the token to.");
            return false;
        }
        candidates.forEach(candidateId -> createAttachedToken(gameData, entry, e, candidateId));
        return true;
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
        int createdBefore = entry.getCreatedPermanentIds().size();
        createAttachedToken(gameData, entry, context.effect(), chosenPermanentId);
        recordCreated(gameData, entry, createdBefore);
    }

    private List<UUID> triggeringCandidates(GameData gameData, StackEntry entry,
                                            CreateTokenAttachedToTargetEffect e) {
        List<UUID> triggeringIds = subjectResolutionService.permanentIds(gameData, entry, e, Subjects.triggeringAll());
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
