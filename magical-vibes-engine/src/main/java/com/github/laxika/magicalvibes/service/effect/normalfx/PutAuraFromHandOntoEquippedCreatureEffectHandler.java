package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoEquippedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Holy Avenger's Aura-from-hand combat-damage trigger. */
@Component
@RequiredArgsConstructor
public class PutAuraFromHandOntoEquippedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAuraFromHandOntoEquippedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent equipmentSnapshot = entry.getSourcePermanentSnapshot();
        UUID equippedCreatureId = equipmentSnapshot != null && equipmentSnapshot.getAttachedTo() != null
                ? equipmentSnapshot.getAttachedTo()
                : equipment == null ? null : equipment.getAttachedTo();
        if (equippedCreatureId == null) {
            return;
        }

        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equippedCreatureId);
        if (equippedCreature == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        List<Integer> auraIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (card.isAura() && auraAttachmentService.canEnchant(
                        gameData, card, entry.getControllerId(), equippedCreature)) {
                    auraIndices.add(i);
                }
            }
        }

        if (auraIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + " has no Aura cards in hand."));
            return;
        }

        String prompt = "Choose an Aura card from your hand to put onto the battlefield attached to "
                + equippedCreature.getCard().getName() + ".";
        playerInputService.beginTargetedCardChoice(
                gameData, entry.getControllerId(), auraIndices, prompt, equippedCreature.getId());
    }
}
