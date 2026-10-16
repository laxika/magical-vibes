package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BattleDefeatedExileAndCastTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Siege defeat: "you may cast it transformed without paying its mana cost" (CR 310.12b). Accepting
 * casts the exiled battle's back face; declining leaves the battle in exile.
 */
@Component
public class BattleDefeatedCastTransformedMayHandler implements MayEffectHandlerBean {

    private final BattleDefeatSupport battleDefeatSupport;
    private final InputCompletionService inputCompletionService;

    public BattleDefeatedCastTransformedMayHandler(BattleDefeatSupport battleDefeatSupport,
                                                   @Lazy InputCompletionService inputCompletionService) {
        this.battleDefeatSupport = battleDefeatSupport;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BattleDefeatedExileAndCastTransformedEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            battleDefeatSupport.castExiledBattleTransformed(gameData, ability.controllerId(), ability.sourceCard());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
