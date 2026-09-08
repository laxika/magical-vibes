package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReanimateEnchantedCreatureCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the enter-the-battlefield ability shared by graveyard-enchanting reanimation Auras. */
@Component
@RequiredArgsConstructor
public class ReanimateEnchantedCreatureCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final AuraAttachmentService auraAttachmentService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReanimateEnchantedCreatureCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reanimate = (ReanimateEnchantedCreatureCardEffect) effect;
        Permanent aura = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, aura.getAttachedTo());
        if (creatureCard == null) {
            return;
        }

        Permanent creature = graveyardReturnSupport.reanimateTargetedCard(
                gameData, entry.getControllerId(), creatureCard, reanimate.enterTapped(),
                enteringCreature -> aura.setAttachedTo(enteringCreature.getId()));
        if (creature == null) {
            aura.setAttachedTo(null);
            return;
        }

        if (!auraAttachmentService.canEnchant(gameData, aura.getCard(), entry.getControllerId(), creature)) {
            aura.setAttachedTo(null);
            destructionSupport.sacrificeAndLog(gameData, creature, entry.getControllerId());
            return;
        }

        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.builder()
                .card(aura.getCard())
                .text(" becomes attached to ")
                .card(creature.getCard())
                .text(".")
                .build());
    }
}
