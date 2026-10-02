package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Resolves Fallaji Antiquarian's targeted graveyard duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffectHandler
        implements NormalEffectHandlerBean {

    private static final String UNEARTH_COST = "{1}{R}";

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForBoundEffectGroup(effect);
        if (targetIds == null) {
            targetIds = entry.targetsForEffect(effect);
        }
        if (targetIds.isEmpty()) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        if (target == null
                || gameQueryService.isToken(gameData, target)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))
                || Objects.equals(entry.getSourcePermanentId(), target.getId())
                || (!gameQueryService.isCreature(gameData, target) && !gameQueryService.isArtifact(gameData, target))) {
            return;
        }

        Card duplicate = target.getCard().createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        if (!graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), duplicate)) {
            return;
        }

        gameData.perpetualGraveyardAbilities.put(
                duplicate.getId(), List.of(Card.unearthAbility(UNEARTH_COST)));
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " conjures a duplicate of ", target.getCard(),
                " into your graveyard with unearth " + UNEARTH_COST + "."));
    }
}
