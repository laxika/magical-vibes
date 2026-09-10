package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherCreaturesOfChosenManaValueParityEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyOtherCreaturesOfChosenManaValueParityEffect} through a two-phase odd/even
 * choice, then destroys matching creatures on every battlefield except the source permanent.
 */
@Component
@RequiredArgsConstructor
public class DestroyOtherCreaturesOfChosenManaValueParityEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyOtherCreaturesOfChosenManaValueParityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellManaValueParity == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellManaValueParityChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        ManaValueParity chosen = gameData.chosenSpellManaValueParity;
        gameData.chosenSpellManaValueParity = null;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        PermanentIsCreaturePredicate creaturePredicate = new PermanentIsCreaturePredicate();

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (entry.getSourcePermanentId() != null
                        && entry.getSourcePermanentId().equals(permanent.getId())) {
                    continue;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, creaturePredicate, filterContext)
                        && chosen.matches(permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue())) {
                    toDestroy.add(permanent);
                }
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}
