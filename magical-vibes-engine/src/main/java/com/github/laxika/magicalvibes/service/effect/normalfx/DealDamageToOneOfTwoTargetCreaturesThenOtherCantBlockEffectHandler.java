package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect) effect;
        List<UUID> legal = legalTargets(gameData, entry);
        if (legal.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " fizzles (no legal targets)."));
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluated = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);
        UUID choosingPlayerId = gameQueryService.findPermanentController(gameData, legal.getFirst());

        if (legal.size() == 1) {
            resolveChoice(gameData, entry, legal.getFirst(), null, damage);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.DealDamageToOneOfTwoThenOtherCantBlock(
                choosingPlayerId, entry.getCard(), entry.getControllerId(), legal.get(0), legal.get(1), damage));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, legal,
                "Choose a creature to receive damage. The other can't block this turn.");
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(choosingPlayerId)
                + " must choose one of the two creatures targeted by " + entry.getCard().getName() + "."));
    }

    private List<UUID> legalTargets(GameData gameData, StackEntry entry) {
        List<UUID> legal = new ArrayList<>();
        UUID sharedControllerId = null;
        List<UUID> targets = entry.getTargetIds();
        if (targets == null) {
            return legal;
        }

        for (UUID targetId : targets) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null || controllerId.equals(entry.getControllerId())) {
                continue;
            }
            if (sharedControllerId == null) {
                sharedControllerId = controllerId;
            }
            if (!sharedControllerId.equals(controllerId)) {
                continue;
            }
            legal.add(targetId);
        }
        return legal;
    }

    private void resolveChoice(GameData gameData, StackEntry entry, UUID chosenId, UUID otherId, int damage) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
        if (chosen == null) {
            return;
        }
        if (!damageSupport.isDamagePreventedForCreature(gameData, entry, chosen)) {
            damageSupport.dealCreatureDamage(gameData, entry, chosen, damage);
        }

        if (otherId != null) {
            Permanent other = gameQueryService.findPermanentById(gameData, otherId);
            if (other != null) {
                other.setCantBlockThisTurn(true);
                gameLogService.append(gameData, GameLog.cardThen(other.getCard(), " can't block this turn."));
            }
        }
    }
}
