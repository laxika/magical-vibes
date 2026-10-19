package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyEnchantedInstantAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Creates and parks the copy made by Spellweaver Volute. */
@Component
public class CopyEnchantedInstantAndMayCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final ExileService exileService;

    public CopyEnchantedInstantAndMayCastCopyEffectHandler(GameQueryService gameQueryService,
                                                           GameLogService gameLogService,
                                                           CopySupport copySupport,
                                                           ExileService exileService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.copySupport = copySupport;
        this.exileService = exileService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyEnchantedInstantAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyEnchantedInstantAndMayCastCopyEffect copyEffect =
                (CopyEnchantedInstantAndMayCastCopyEffect) effect;
        // The enchanted card is read from Volute's last existence on the battlefield, so a Volute that
        // left in response still identifies the card it was enchanting (even if that card has since been
        // exiled), while one that fell off because its card left enchanted nothing.
        Permanent volute = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent lastKnownVolute = volute != null ? volute : entry.getSourcePermanentSnapshot();
        UUID enchantedCardId = lastKnownVolute != null ? lastKnownVolute.getAttachedTo() : copyEffect.enchantedCardId();
        Card enchantedCard = enchantedCardId == null ? null
                : volute != null ? gameQueryService.findCardInGraveyardById(gameData, enchantedCardId)
                : gameQueryService.findCardById(gameData, enchantedCardId);
        if (enchantedCard == null || !enchantedCard.hasType(com.github.laxika.magicalvibes.model.CardType.INSTANT)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no enchanted instant card to copy."));
            return;
        }

        if (!gameQueryService.canPlayerCastSpellsFromZone(
                gameData, entry.getControllerId(), com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
            // The copy is created in the graveyard, where a casting restriction keeps it from being cast.
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " can't cast a copy of the enchanted instant from a graveyard."));
            return;
        }

        Card copy = copySupport.createCopyCard(enchantedCard);
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(new CopyEnchantedInstantAndMayCastCopyEffect(enchantedCard.getId())),
                "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                copy.getId(),
                null,
                entry.getSourcePermanentId()
        ));
    }
}
