package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPermanentScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToPermanentsTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPermanentsTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPermanentsTargetControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (!inScope(gameData, permanent, e.scope())) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, permanent, entry.getCard(), entry.getControllerId())) {
                    gameLogService.append(gameData, GameLog.textCardText(cardName + "'s damage to ", permanent.getCard(), " is prevented."));
                    continue;
                }
                int markedBefore = permanent.getMarkedDamage();
                damageSupport.dealCreatureDamage(gameData, entry, permanent, rawDamage);
                // "Each creature dealt damage this way …" (Aggravate): a creature whose damage was
                // fully prevented or redirected away never took damage, so it isn't affected.
                if (e.damagedCreaturesMustAttackThisTurn() && permanent.getMarkedDamage() > markedBefore) {
                    permanent.setMustAttackThisTurn(true);
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private boolean inScope(GameData gameData, Permanent permanent, DamagedPermanentScope scope) {
        if (gameQueryService.isCreature(gameData, permanent)) {
            return true;
        }
        return scope == DamagedPermanentScope.CREATURES_AND_PLANESWALKERS
                && permanent.getCard().hasType(CardType.PLANESWALKER);
    }
}
