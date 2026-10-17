package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToOpponentCreatureOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToOpponentCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) effect;

        UUID auraCardId = entry.getCard().getId();
        UUID auraControllerId = entry.getControllerId();
        UUID enchantedCreatureControllerId = e.enchantedCreatureControllerId();

        if (enchantedCreatureControllerId == null) {
            log.info("Game {} - {} death trigger fizzles (no enchanted creature controller)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find the aura card in the graveyard
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} not found in graveyard, death trigger fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // The target was chosen by the dying creature's controller when the trigger went on the stack.
        Permanent target = entry.getTargetId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        UUID targetControllerId = target == null
                ? null : gameQueryService.findPermanentController(gameData, target.getId());
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || enchantedCreatureControllerId.equals(targetControllerId)
                || !auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, target)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (target is no longer legal)."));
            log.info("Game {} - {} death trigger fizzles (target no longer legal)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(target.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPerm);

        String controllerName = gameData.playerIdToName.get(auraControllerId);
        gameLogService.append(gameData, GameLog.builder().card(auraCard).text(" returns to the battlefield attached to ").card(target.getCard()).text(" under " + controllerName + "'s control.").build());
        log.info("Game {} - {} returns attached to {}",
                gameData.id, auraCard.getName(), target.getCard().getName());
    }
}
