package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscordRandomCardCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Discord, Lord of Disharmony's random card-name copy ability. */
@Component
@RequiredArgsConstructor
public class DiscordRandomCardCopyEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final CopySupport copySupport;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    private volatile List<CardPrinting> nonlandNamePool;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscordRandomCardCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardPrinting> pool = nonlandNamePool();
        if (pool.isEmpty()) {
            return;
        }

        CardPrinting printing = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        Card copy = copySupport.createCopyCard(printing.createCard());
        UUID controllerId = entry.getControllerId();
        exileService.exileCard(gameData, controllerId, copy);
        exileSupport.grantPlayUntilOwnersNextEndStep(gameData, copy.getId(), controllerId);
        gameData.exilePlayAnyManaType.add(copy.getId());
        if (entry.getSourcePermanentId() != null) {
            gameData.discordCopySourcePermanents.put(copy.getId(), entry.getSourcePermanentId());
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "Discord chooses ", copy, ". A copy may be cast until your next end step."));
    }

    private List<CardPrinting> nonlandNamePool() {
        List<CardPrinting> cached = nonlandNamePool;
        if (cached != null) {
            return cached;
        }

        Map<String, CardPrinting> byName = new LinkedHashMap<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                Card card = printing.createCard();
                if (card.getName() != null && !card.hasType(CardType.LAND)) {
                    byName.putIfAbsent(card.getName(), printing);
                }
            }
        }
        cached = List.copyOf(new ArrayList<>(byName.values()));
        nonlandNamePool = cached;
        return cached;
    }
}
