package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureRestCantBlockEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class TargetPlayerChoosesCreatureRestCantBlockEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCreatureRestCantBlockEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    creatureIds.add(perm.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            restrictOtherCreatures(gameData, targetPlayerId, null);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, creatureIds, 1,
                new MultiPermanentChoiceContext.ChooseCreatureRestCantBlock(targetPlayerId),
                "Choose a creature to keep able to block. Your other creatures can't block this turn.");
    }

    public static void restrictOtherCreatures(GameData gameData, UUID playerId, UUID keptId) {
        PermanentPredicate predicate = new PermanentControlledByPlayerPredicate(playerId);
        if (keptId != null) {
            predicate = new PermanentAllOfPredicate(List.of(predicate,
                    new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(keptId))));
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                "Other creatures can't block", null, playerId,
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(predicate,
                        new PermanentTruePredicate(), "can't block this turn"),
                null, null, predicate, EffectDuration.UNTIL_END_OF_TURN, 0));
    }
}
