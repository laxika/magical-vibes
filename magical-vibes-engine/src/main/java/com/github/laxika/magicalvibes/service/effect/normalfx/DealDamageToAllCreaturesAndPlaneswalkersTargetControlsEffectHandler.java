package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            List<Permanent> destroyed = new ArrayList<>();
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                boolean isCreature = gameQueryService.isCreature(gameData, permanent);
                boolean isPlaneswalker = permanent.getCard().hasType(CardType.PLANESWALKER);
                if (!isCreature && !isPlaneswalker) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, permanent, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + permanent.getCard().getName() + " is prevented.");
                    continue;
                }
                if (damageSupport.dealCreatureDamage(gameData, entry, permanent, rawDamage)) {
                    destroyed.add(permanent);
                }
            }
            damageSupport.destroyAllLethal(gameData, destroyed);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
