package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingSpellweaverVoluteReattachment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SpellweaverVoluteEnterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Coordinates Spellweaver Volute's post-cast exile and graveyard reattachment choice. */
@Component
public class SpellweaverVoluteSupport {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    public SpellweaverVoluteSupport(GameQueryService gameQueryService,
                                    PermanentRemovalService permanentRemovalService,
                                    ExileService exileService,
                                    GameLogService gameLogService,
                                    PlayerInputService playerInputService) {
        this.gameQueryService = gameQueryService;
        this.permanentRemovalService = permanentRemovalService;
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
    }

    public boolean isSpellweaverVolute(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(SpellweaverVoluteEnterEffect.class::isInstance)
                || card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(SpellweaverVoluteEnterEffect.class::isInstance);
    }

    /**
     * Completes the immediate part of Volute's replacement clause after its copied spell is cast.
     * Returns true when the free-cast queue must remain paused for the reattachment choice.
     */
    public boolean handleSuccessfulCopyCast(GameData gameData, UUID copyCardId) {
        PendingSpellweaverVoluteReattachment pending = gameData.pendingSpellweaverVoluteReattachment;
        if (pending == null || !Objects.equals(pending.copyCardId(), copyCardId)) {
            return false;
        }

        Card enchantedCard = gameQueryService.findCardInGraveyardById(gameData, pending.enchantedCardId());
        Permanent aura = gameQueryService.findPermanentById(gameData, pending.auraPermanentId());
        if (enchantedCard != null) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, enchantedCard.getId());
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, enchantedCard.getId());
            exileService.exileCard(gameData, ownerId, enchantedCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text("Spellweaver Volute exiles ")
                    .card(enchantedCard)
                    .text(" from a graveyard.")
                    .build());
        }

        gameData.pendingSpellweaverVoluteReattachment = new PendingSpellweaverVoluteReattachment(
                null, pending.auraPermanentId(), pending.enchantedCardId(), pending.controllerId());
        if (aura == null || enchantedCard == null
                || !Objects.equals(aura.getAttachedTo(), pending.enchantedCardId())) {
            gameData.pendingSpellweaverVoluteReattachment = null;
            return false;
        }

        aura.setAttachedTo(null);
        List<Card> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                if (card.hasType(CardType.INSTANT)) {
                    candidates.add(card);
                }
            }
        }
        if (candidates.isEmpty()) {
            gameData.pendingSpellweaverVoluteReattachment = null;
            return false;
        }

        playerInputService.beginMultiGraveyardChoice(
                gameData, pending.controllerId(), candidates, 1, 1,
                "Choose another instant card in a graveyard for Spellweaver Volute.");
        return true;
    }

    /** Finishes Volute's mandatory reattachment choice. */
    public void completeAttachmentChoice(GameData gameData, UUID chosenCardId) {
        PendingSpellweaverVoluteReattachment pending = gameData.pendingSpellweaverVoluteReattachment;
        if (pending == null) {
            return;
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, pending.auraPermanentId());
        Card chosenCard = gameQueryService.findCardInGraveyardById(gameData, chosenCardId);
        if (aura != null && chosenCard != null && chosenCard.hasType(CardType.INSTANT)) {
            aura.setAttachedTo(chosenCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text("Spellweaver Volute becomes attached to ")
                    .card(chosenCard)
                    .text(" in a graveyard.")
                    .build());
        }
        gameData.pendingSpellweaverVoluteReattachment = null;
    }

    /** Clears state when the copy was declined or could not be cast. */
    public void clearIfUncast(GameData gameData) {
        PendingSpellweaverVoluteReattachment pending = gameData.pendingSpellweaverVoluteReattachment;
        if (pending != null && pending.copyCardId() != null) {
            gameData.pendingSpellweaverVoluteReattachment = null;
        }
    }
}
