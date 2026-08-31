package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the conditional blink-or-token effect used by Unyielding Gatekeeper. */
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect) effect;
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        UUID abilityControllerId = entry.getControllerId();
        boolean controlledByAbilityController = abilityControllerId != null
                && abilityControllerId.equals(targetControllerId);
        Card card = target.getOriginalCard();
        UUID ownerId = gameData.stolenCreatures.getOrDefault(targetId, targetControllerId);

        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));

        if (controlledByAbilityController && !card.isToken()) {
            gameData.removeFromExile(card.getId());
            Permanent returned = new Permanent(card);
            returned.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, abilityControllerId, returned);
            if (!abilityControllerId.equals(ownerId)) {
                graveyardReturnSupport.trackStolenCreature(gameData, returned.getId(),
                        abilityControllerId, ownerId);
            }
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, abilityControllerId, card, null, false);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " returns to the battlefield tapped under "
                            + gameData.playerIdToName.get(abilityControllerId) + "'s control."));
            return;
        }

        if (!controlledByAbilityController && targetControllerId != null) {
            destructionSupport.createTokenForPlayer(gameData, targetControllerId,
                    exile.tokenForOtherController(), entry.getCard().getName(), entry.getCard().getSetCode());
        }
    }
}
