package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Answers single permanent / any-target picks: the AI prefers the opponent's strongest
 * creature (by effective power), then the opponent's highest-mana-value permanent, then its
 * own cheapest permanent, then the first valid ID. Ported verbatim from the legacy
 * {@code AiChoiceHandler.handlePermanentChoice} heuristic.
 *
 * <p>Sacrifice costs on activated abilities additionally avoid sacrificing the ability's
 * source while other legal fodder remains (keep Viscera Seer online to sac the rest).
 */
@Slf4j
class PermanentChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.PermanentChoice> {

    @Override
    public Class<PendingInteraction.PermanentChoice> handledType() {
        return PendingInteraction.PermanentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PermanentChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        Set<UUID> validIds = interaction.validIds();
        if (validIds.isEmpty()) {
            return;
        }

        GameData gameData = ctx.gameData();
        List<Permanent> opponentField = gameData.getOpponentBattlefield(ctx.aiPlayerId());
        List<Permanent> ownField = gameData.playerBattlefields.getOrDefault(ctx.aiPlayerId(), List.of());

        // Activated-ability sacrifice costs: keep the outlet, sac cheapest other fodder first
        UUID abilitySourceId = sacrificeAbilitySourceToPreserve(interaction.context());
        if (abilitySourceId != null) {
            UUID fodder = ownField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .filter(p -> !p.getId().equals(abilitySourceId))
                    .min(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(null);
            if (fodder != null) {
                log.info("AI: Choosing sacrifice fodder {} (preserving outlet {}) in game {}",
                        fodder, abilitySourceId, ctx.gameId());
                ctx.gameActions().answerInteraction(ctx.selfConnection(),
                        new InteractionAnswer.PermanentChosen(fodder));
                return;
            }
        }

        // Try opponent's best creature first
        UUID best = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .filter(p -> ctx.gameQueryService().isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> ctx.gameQueryService().getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);

        if (best == null) {
            best = opponentField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        if (best == null) {
            best = ownField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .min(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(validIds.iterator().next());
        }

        log.info("AI: Choosing permanent {} in game {}", best, ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.PermanentChosen(best));
    }

    /**
     * When paying an activated ability's sacrifice cost, returns the ability source id so the
     * AI can prefer other creatures as fodder and keep the outlet available.
     */
    private static UUID sacrificeAbilitySourceToPreserve(PermanentChoiceContext context) {
        if (!(context instanceof PermanentChoiceContext.ActivatedAbilityCostChoice costChoice)) {
            return null;
        }
        if (!(costChoice.costEffect() instanceof CostEffect cost)) {
            return null;
        }
        if (!cost.sacrificesChosenCreature() && cost.consumedPermanentFilter() == null) {
            return null;
        }
        return costChoice.sourcePermanentId();
    }
}
