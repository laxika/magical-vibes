package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCreatureToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

/** Resolves Ace Flockbringer's perpetual flying duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringCreatureToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CopySupport copySupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(gameData, triggeringCardId);
        if (triggeringSpell == null || triggeringSpell.getCard() == null
                || !triggeringSpell.getCard().hasType(CardType.CREATURE)) {
            return;
        }

        Card duplicate = copySupport.createCopyCard(triggeringSpell.getCard());
        EnumSet<Keyword> keywords = duplicate.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(duplicate.getKeywords());
        keywords.add(Keyword.FLYING);
        duplicate.setKeywords(keywords);
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);

        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " conjures a duplicate of "
                        + triggeringSpell.getCard().getName() + " into their hand."));
    }
}
