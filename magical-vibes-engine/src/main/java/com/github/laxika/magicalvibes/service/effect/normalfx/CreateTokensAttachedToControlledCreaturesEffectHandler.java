package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedTokensToHostsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttachedToControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link CreateTokensAttachedToControlledCreaturesEffect}: one token for each matching creature that its
 * token could enchant, created as one ordered event and attached by an {@link AttachCreatedTokensToHostsEffect} step
 * inserted after this one.
 */
@Component
@RequiredArgsConstructor
public class CreateTokensAttachedToControlledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final AttachCreatedTokensToHostsEffectHandler attachCreatedTokensToHostsEffectHandler;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAttachedToControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensAttachedToControlledCreaturesEffect) effect;
        List<Permanent> candidates = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, e, e.controllers())) {
            candidates.addAll(gameData.playerBattlefields.getOrDefault(playerId, List.of()));
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Permanent> matchingCreatures = candidates.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.filter(), filterContext))
                .toList();
        if (matchingCreatures.isEmpty()) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);
        Card preview = TokenCardFactory.create(e.token(), power, toughness, entry.getCard().getSetCode());
        Permanent previewPermanent = new Permanent(preview);

        List<Permanent> eligibleCreatures = matchingCreatures.stream()
                .filter(creature -> auraAttachmentService.canEnchant(
                        gameData, preview, entry.getControllerId(), creature))
                .filter(creature -> !gameQueryService.hasProtectionFromSource(
                        gameData, creature, previewPermanent))
                .toList();
        if (eligibleCreatures.isEmpty()) {
            return;
        }

        // The Roles are one creation event whose replacements the creating player orders (CR 616.1), so it may stop
        // for that choice; they are attached by the next step, once the event has made them.
        int createdFrom = entry.getCreatedPermanentIds().size();
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), eligibleCreatures.size(),
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);
        AttachCreatedTokensToHostsEffect attach = new AttachCreatedTokensToHostsEffect(createdFrom,
                eligibleCreatures.stream().map(Permanent::getId).toList());
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(attach));
        } else {
            attachCreatedTokensToHostsEffectHandler.resolve(gameData, entry, attach);
        }
    }
}
