package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChangeTargetOfTargetSpellWithSingleTargetEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellUnlessControllerPaysHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ChangeTargetOfTargetSpellWithSingleTargetEffectHandler changeTargetHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellUnlessControllerPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(ChangeTargetOfTargetSpellUnlessControllerPaysEffect.class::isInstance)
                .map(ChangeTargetOfTargetSpellUnlessControllerPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, ability.targetCardId());
        if (targetSpell == null || !targetSpell.isSingleTarget()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String manaCost = "{" + effect.payAmount() + "}";
        ManaCost cost = new ManaCost(manaCost);
        if (accepted && cost.canPay(gameData.playerManaPools.get(player.getId()))) {
            cost.pay(gameData.playerManaPools.get(player.getId()));
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + manaCost + " to keep ", targetSpell.getCard(), "'s target."));
        } else {
            changeTargetHandler.resolve(gameData, ability.sourceControllerId(), ability.targetCardId(),
                    ability.sourceCard(), new ChangeTargetOfTargetSpellWithSingleTargetEffect());
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
