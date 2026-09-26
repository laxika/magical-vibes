package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FalseOrdersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FalseOrdersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CombatRemovalSupport combatRemovalSupport;
    private final BalduvianWarlordEffectHandler balduvianWarlordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FalseOrdersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        combatRemovalSupport.removeFromCombatAndUnblockSoleBlockers(gameData, entry, target);
        if (!balduvianWarlordEffectHandler.hasLegalAttacker(gameData, target)) {
            return;
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(BalduvianWarlordEffect.forReblockingOnly()),
                "You may have it block an attacking creature of your choice?",
                target.getId()
        ));
    }
}
