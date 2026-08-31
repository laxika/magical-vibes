package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttachedToControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensAttachedToControlledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAttachedToControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensAttachedToControlledCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Permanent> matchingCreatures = List.copyOf(battlefield).stream()
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

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId(), false);
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), eligibleCreatures.size(),
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        int tokenIndex = 0;
        for (Permanent creature : eligibleCreatures) {
            for (int copy = 0; copy < tokenMultiplier && tokenIndex < createdIds.size(); copy++) {
                attachToken(gameData, entry, createdIds.get(tokenIndex++), creature);
            }
        }
    }

    private void attachToken(GameData gameData, StackEntry entry, UUID tokenId, Permanent creature) {
        Permanent role = gameQueryService.findPermanentById(gameData, tokenId);
        if (role == null || !auraAttachmentService.canEnchant(
                gameData, role.getCard(), entry.getControllerId(), creature)) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(role.getId());
        role.setAttachedTo(creature.getId());
        role.setTimestamp(gameData.nextTimestamp());
        triggerCollectionService.checkAuraAttachedTriggers(gameData, role, creature.getId());
    }
}
