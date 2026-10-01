package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardOntoTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Pampered Loamfrill's modified graveyard duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetCreatureCardOntoTopOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetCreatureCardOntoTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null
                || !targetCard.hasType(CardType.CREATURE)
                || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetCardId))
                || (entry.getCard() != null && targetCardId.equals(entry.getCard().getId()))) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        Card duplicate = targetCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.perpetualCardPowerToughnessModifiers.merge(
                duplicate.getId(),
                new CardPowerToughnessModifier(1, 1),
                (current, added) -> current.add(added.power(), added.toughness()));
        gameData.perpetualCardKeywords.merge(duplicate.getId(), Set.of(Keyword.DEATHTOUCH),
                (existing, added) -> {
                    Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
                    merged.addAll(existing);
                    merged.addAll(added);
                    return Set.copyOf(merged);
                });
        library.addFirst(duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", targetCard, " is conjured onto the top of your library."));
    }
}
