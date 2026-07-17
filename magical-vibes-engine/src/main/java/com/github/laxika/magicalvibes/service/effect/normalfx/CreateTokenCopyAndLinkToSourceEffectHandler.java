package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Dance of Many's enter-the-battlefield ability: create a token copy of the target nontoken
 * creature and forge the mutual leaves-the-battlefield bond between the enchantment and the token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyAndLinkToSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyAndLinkToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent sourceEnchantment = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourceEnchantment == null) {
            log.info("Game {} - Dance of Many enchantment no longer on battlefield, no token created", gameData.id);
            return;
        }

        Permanent targetCreature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (targetCreature == null) {
            log.info("Game {} - Target creature no longer on battlefield, no token created", gameData.id);
            return;
        }

        // Build the token as a plain copy (no P/T or type overrides), then attach the reciprocal
        // "when this leaves the battlefield, sacrifice the linked enchantment" trigger before freezing.
        Card sourceCard = targetCreature.getCard();
        Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                sourceCard, new CreateTokenCopyOfTargetPermanentEffect());
        tokenCard.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE));

        Permanent tokenPermanent = new Permanent(tokenCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

        // Forge the bond: each permanent remembers the other so their leaves-battlefield triggers can
        // find their partner.
        sourceEnchantment.setChosenPermanentId(tokenPermanent.getId());
        tokenPermanent.setChosenPermanentId(sourceEnchantment.getId());

        String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - Dance of Many creates a token copy of {}", gameData.id, sourceCard.getName());

        // The token wasn't cast; any targeted ETB ability of the copied creature chooses its target at
        // trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, entry.getControllerId(), tokenCard, null, false);
    }
}
