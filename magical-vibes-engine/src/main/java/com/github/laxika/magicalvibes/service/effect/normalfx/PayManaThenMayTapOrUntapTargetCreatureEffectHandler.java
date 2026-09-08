package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaThenMayTapOrUntapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayManaThenMayTapOrUntapTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaThenMayTapOrUntapTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var payThenTap = (PayManaThenMayTapOrUntapTargetCreatureEffect) effect;

        if (gameData.resolvedMayAccepted != null) {
            boolean paid = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (paid) {
                queueReflexiveTrigger(gameData, entry);
            }
            return;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(),
                entry.getCard().getName() + " - Pay " + payThenTap.manaCost() + " to use its ability?",
                null,
                payThenTap.manaCost(),
                entry.getSourcePermanentId()));
    }

    private void queueReflexiveTrigger(GameData gameData, StackEntry entry) {
        CardEffect tapOrUntap = new TapOrUntapTargetPermanentEffect(new PermanentIsCreaturePredicate());
        MayEffect mayTapOrUntap = new MayEffect(tapOrUntap, "Tap or untap target creature?");
        TargetSpec targetSpec = mayTapOrUntap.targetSpec();
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        List<UUID> validTargets = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (targetPredicateEvaluationService.matchesPermanent(targetPredicate, permanent, filterContext)) {
                    validTargets.add(permanent.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(mayTapOrUntap)));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargets,
                entry.getCard().getName() + "'s reflexive ability - Choose target.");
    }
}
