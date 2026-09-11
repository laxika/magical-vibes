package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndCastUpToEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Baron Helmut Zemo's copied-spell offer. */
@Component
@RequiredArgsConstructor
public class CopyCardsExiledWithSourceAndCastUpToEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCardsExiledWithSourceAndCastUpToEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyCardsExiledWithSourceAndCastUpToEffect copyEffect =
                (CopyCardsExiledWithSourceAndCastUpToEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<UUID> copyIds = new ArrayList<>();
        for (UUID cardId : entry.getActivatedAbilityExiledCardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || !isSpell(exiled.card()) || exiled.card().isCantBeCopied()) {
                continue;
            }
            Card copy = copySupport.createCopyCard(exiled.card());
            exileService.exileCard(gameData, entry.getControllerId(), copy);
            copyIds.add(copy.getId());
            gameLogService.append(gameData, GameLog.textCardText(
                    entry.getCard().getName() + " creates a copy of ", exiled.card(), "."));
        }

        if (copyIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        entry.getControllerId(), copyIds,
                        Math.min(copyEffect.maxCount(), copyIds.size()),
                        "You may cast up to " + copyEffect.maxCount()
                                + " of these copies without paying their mana costs.", true));
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
