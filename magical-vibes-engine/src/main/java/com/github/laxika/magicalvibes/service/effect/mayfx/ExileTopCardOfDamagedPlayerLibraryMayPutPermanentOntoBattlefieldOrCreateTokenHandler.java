package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutExiledCardOntoBattlefieldUnderControllerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Completes Fishing Gear's optional permanent theft and Fish fallback. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenHandler
        implements MayEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final InputCompletionService inputCompletionService;
    private final PutExiledCardOntoBattlefieldUnderControllerEffectHandler putExiledCardHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect.class::isInstance)
                .map(ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && ability.targetCardId() != null
                && gameData.findExiledCard(ability.targetCardId()) != null) {
            var putEffect = new PutExiledCardOntoBattlefieldUnderControllerEffect(ability.targetCardId());
            var entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    List.of(putEffect),
                    null,
                    ability.sourcePermanentId());
            putExiledCardHandler.resolve(gameData, entry, putEffect);
        } else {
            var tokenEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    List.of(effect.fallbackToken()),
                    null,
                    ability.sourcePermanentId());
            createTokenEffectHandler.resolveForController(
                    gameData, tokenEntry, effect.fallbackToken(), ability.controllerId());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
