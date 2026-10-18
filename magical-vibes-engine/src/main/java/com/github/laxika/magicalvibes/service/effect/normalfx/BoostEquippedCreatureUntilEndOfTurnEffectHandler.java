package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostEquippedCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEquippedCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostEquippedCreatureUntilEndOfTurnEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Equipment";

        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // A trigger granted by an attachment ("it gets ...") boosts the creature that triggered it,
        // even if the attachment moved or left the battlefield before resolution.
        UUID attachedId = entry.getAttachedPermanentSnapshot() != null ? entry.getAttachedPermanentSnapshot().getId()
                : equipment != null ? equipment.getAttachedTo()
                : entry.getSourcePermanentSnapshot() != null
                ? entry.getSourcePermanentSnapshot().getAttachedTo() : entry.getTargetId();
        Permanent equippedCreature = attachedId == null ? null : gameQueryService.findPermanentById(gameData, attachedId);

        if (e.orControlledCreatureMatching() != null) {
            List<UUID> candidateIds = new ArrayList<>();
            if (equippedCreature != null) {
                candidateIds.add(equippedCreature.getId());
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
                if (!candidateIds.contains(permanent.getId())
                        && gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, e.orControlledCreatureMatching())) {
                    candidateIds.add(permanent.getId());
                }
            }
            if (candidateIds.isEmpty()) {
                log.info("Game {} - {} trigger has no creature to boost", gameData.id, sourceName);
                return;
            }
            if (candidateIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.BoostEquippedOrMatchingCreatureChoice(entry, e));
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidateIds,
                        "Choose the creature that gets the boost.");
                return;
            }
            equippedCreature = gameQueryService.findPermanentById(gameData, candidateIds.getFirst());
        } else if (attachedId == null) {
            // The source left the battlefield (e.g. its own sacrifice cost) and no attached
            // permanent was captured at activation.
            log.info("Game {} - {} trigger fizzles: equipment no longer attached", gameData.id, sourceName);
            return;
        }
        if (equippedCreature == null) {
            log.info("Game {} - {} trigger fizzles: equipped creature no longer on battlefield", gameData.id, sourceName);
            return;
        }

        applyBoost(gameData, entry, e, equippedCreature);
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.BoostEquippedOrMatchingCreatureChoice context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null) {
            return;
        }
        applyBoost(gameData, context.resolvingEntry(), context.effect(), creature);
    }

    private void applyBoost(GameData gameData, StackEntry entry, BoostEquippedCreatureUntilEndOfTurnEffect e,
                            Permanent equippedCreature) {
        AmountContext ctx = AmountContext.forStackEntry(entry, equippedCreature);
        int powerBoost = amountEvaluationService.evaluate(gameData, e.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, e.toughnessBoost(), ctx);

        equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + powerBoost);
        equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + toughnessBoost);

        gameLogService.append(gameData, GameLog.builder()
                .card(equippedCreature.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                .build());
        log.info("Game {} - {} gets {}/{} until end of turn", gameData.id,
                equippedCreature.getCard().getName(), powerBoost, toughnessBoost);
    }
}
