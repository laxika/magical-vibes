package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BoostTargetCreaturePerChosenTypeCountEffect} (Pack's Disdain): prompts the
 * controller to choose a creature type, then applies a per-count modifier to the target creature
 * until end of turn, equal to the number of permanents the controller controls of that type
 * (Changeling-aware) times the effect's per-permanent power/toughness.
 *
 * <p>Two-phase: the first resolution begins the creature-type choice and pauses (re-running once the
 * choice completes via {@code rerunCurrentEffectAfterInteraction}); the re-entry reads and clears
 * {@code GameData.chosenSpellSubtype}, counts, and applies the boost. Boost sibling of
 * {@link DealDamageToTargetCreatureEqualToChosenTypeCountEffectHandler}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetCreaturePerChosenTypeCountEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreaturePerChosenTypeCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreaturePerChosenTypeCountEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            // First pass: choose a creature type, then re-run this effect once the choice completes.
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosen = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(chosen);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, predicate, filterContext)) {
                    count++;
                }
            }
        }

        int powerBoost = count * boost.powerPer();
        int toughnessBoost = count * boost.toughnessPer();

        for (UUID targetId : entry.targetsForEffect(effect)) {
            applyBoost(gameData, targetId, powerBoost, toughnessBoost);
        }
    }

    private void applyBoost(GameData gameData, UUID targetId, int powerBoost, int toughnessBoost) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return; // Target left the battlefield before resolution.
        }
        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = String.format("%s gets %+d/%+d until end of turn.",
                target.getCard().getName(), powerBoost, toughnessBoost);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
