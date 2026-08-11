package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellUnlessControllerPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ChangeTargetOfTargetSpellWithSingleTargetEffectHandler changeTargetHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellUnlessControllerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChangeTargetOfTargetSpellUnlessControllerPaysEffect) effect;
        UUID targetCardId = entry.getTargetId();
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetCardId);
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.isSingleTarget()) {
            changeTargetHandler.resolve(gameData, entry, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
            return;
        }

        String manaCost = "{" + e.payAmount() + "}";
        UUID payerId = targetSpell.getControllerId();
        ManaCost cost = new ManaCost(manaCost);
        if (!cost.canPay(gameData.playerManaPools.get(payerId))) {
            changeTargetHandler.resolve(gameData, entry, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(e),
                "Pay " + manaCost + " to keep " + targetSpell.getCard().getName() + " from being retargeted?",
                targetCardId, entry.getControllerId()));
    }
}
