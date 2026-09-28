package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayGetEnergyOrPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EnergyCountersEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExileTopCardMayGetEnergyOrPlayThisTurnHandler implements MayEffectHandlerBean {

    private final EnergyCountersEffectHandler energyCountersEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayGetEnergyOrPlayThisTurnEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExileTopCardMayGetEnergyOrPlayThisTurnEffect effect = ability.effects().stream()
                .filter(ExileTopCardMayGetEnergyOrPlayThisTurnEffect.class::isInstance)
                .map(ExileTopCardMayGetEnergyOrPlayThisTurnEffect.class::cast)
                .findFirst()
                .orElseThrow();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(effect.exiledCardId());
        if (accepted && exiledEntry != null) {
            CardEffect energy = new EnergyCountersEffect(2);
            StackEntry energyEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + " gets two energy counters.",
                    List.of(energy)
            );
            energyCountersEffectHandler.resolve(gameData, energyEntry, energy);
        } else if (!accepted && exiledEntry != null) {
            gameData.exilePlayPermissions.put(exiledEntry.card().getId(), ability.controllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(exiledEntry.card().getId());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
