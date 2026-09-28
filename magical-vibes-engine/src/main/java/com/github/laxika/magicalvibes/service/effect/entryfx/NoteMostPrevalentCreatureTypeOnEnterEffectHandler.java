package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoteMostPrevalentCreatureTypeOnEnterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NoteMostPrevalentCreatureTypeOnEnterEffectHandler implements EntryReplacementHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoteMostPrevalentCreatureTypeOnEnterEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(opponentId);
        if (library == null || library.isEmpty()) {
            return;
        }

        EnumMap<CardSubtype, Integer> counts = new EnumMap<>(CardSubtype.class);
        for (Card card : library) {
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            Set<CardSubtype> creatureTypes = EnumSet.noneOf(CardSubtype.class);
            card.getSubtypes().stream()
                    .filter(gameQueryService::isCreatureSubtype)
                    .forEach(creatureTypes::add);
            if (card.hasKeyword(Keyword.CHANGELING)) {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (gameQueryService.isCreatureSubtype(subtype)) {
                        creatureTypes.add(subtype);
                    }
                }
            }
            creatureTypes.forEach(subtype -> counts.merge(subtype, 1, Integer::sum));
        }

        Map.Entry<CardSubtype, Integer> mostPrevalent = null;
        for (Map.Entry<CardSubtype, Integer> entry : counts.entrySet()) {
            if (mostPrevalent == null
                    || entry.getValue() > mostPrevalent.getValue()
                    || (entry.getValue().equals(mostPrevalent.getValue())
                    && entry.getKey().ordinal() < mostPrevalent.getKey().ordinal())) {
                mostPrevalent = entry;
            }
        }
        if (mostPrevalent != null) {
            enteringPermanent.setChosenSubtype(mostPrevalent.getKey());
        }
    }
}
