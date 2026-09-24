package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceTurnFaceUpCostForPermanentEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a snapshot grant to the controller's current face-down creatures. */
@Component
@RequiredArgsConstructor
public class GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect grant =
                (GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        for (Permanent permanent : List.copyOf(battlefield)) {
            if (!permanent.isFaceDown() || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            permanent.addPersistentTriggeredEffect(EffectSlot.ON_TURNED_FACE_UP,
                    grant.turnFaceUpEffect());
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new ReduceTurnFaceUpCostForPermanentEffect(
                            permanent.getId(), grant.turnFaceUpCostReduction()),
                    permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
