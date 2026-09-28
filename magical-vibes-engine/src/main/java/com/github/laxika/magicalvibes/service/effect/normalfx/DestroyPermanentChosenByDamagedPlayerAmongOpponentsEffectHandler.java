package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the fallback form of a damaged-player opponent-permanent destruction trigger. */
@Component
@RequiredArgsConstructor
public class DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroyEffect = (DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect) effect;
        UUID damagedPlayerId = entry.getTargetId();
        UUID sourceControllerId = entry.getControllerId();
        if (damagedPlayerId == null || sourceControllerId == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(sourceControllerId);
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(sourceControllerId)) {
                return;
            }
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, destroyEffect.predicate(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        });

        if (validIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text("'s ability resolves, but there are no valid permanents to destroy.").build());
            return;
        }

        if (validIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            if (target != null) {
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new com.github.laxika.magicalvibes.model.PermanentChoiceContext.DestroyChosenCreature(
                        damagedPlayerId, entry.getCard().getName()));
        playerInputService.beginPermanentChoice(gameData, damagedPlayerId, validIds,
                "Choose a nonland permanent an opponent controls to destroy.");
    }
}
