package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GainControlOfTargetAuraEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID casterId = entry.getControllerId();
                Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (aura == null) return;

                UUID currentControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
                if (currentControllerId != null && !currentControllerId.equals(casterId)) {
                    gameData.playerBattlefields.get(currentControllerId).remove(aura);
                    gameData.playerBattlefields.get(casterId).add(aura);
                    String casterName = gameData.playerIdToName.get(casterId);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(casterName + " gains control of " , aura.getCard(), "."));
                    log.info("Game {} - {} gains control of {}", gameData.id, casterName, aura.getCard().getName());

                    // A control Aura (e.g. In Bolas's Clutches) grants control to whoever controls
                    // the Aura — its enchanted permanent follows the Aura's new controller.
                    Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
                    if (enchanted != null) {
                        creatureControlService.recomputeControl(gameData, enchanted);
                    }
                }

                TargetFilter auraFilter = aura.getCard().getTargetFilter();
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(aura.getCard().getId())
                        .withSourceControllerId(casterId);
                List<UUID> validTargetIds = new ArrayList<>();
                gameData.forEachPermanent((pid, p) -> {
                    if (p.getId().equals(aura.getId())) return;
                    if (p.getId().equals(aura.getAttachedTo())) return;
                    if (auraFilter != null) {
                        if (predicateEvaluationService.checkTargetFilter(auraFilter, p, filterContext).isPresent()) return;
                    } else if (!gameQueryService.isCreature(gameData, p)) {
                        return;
                    }
                    validTargetIds.add(p.getId());
                });

                if (!validTargetIds.isEmpty()) {
                    gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.AuraGraft(aura.getId()));
                    playerInputService.beginPermanentChoice(gameData, casterId, validTargetIds,
                            "Attach " + aura.getCard().getName() + " to another permanent it can enchant.");
                } else {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(aura.getCard(), " stays attached to its current target (no other valid permanents)."));
                }
    
    }
}
