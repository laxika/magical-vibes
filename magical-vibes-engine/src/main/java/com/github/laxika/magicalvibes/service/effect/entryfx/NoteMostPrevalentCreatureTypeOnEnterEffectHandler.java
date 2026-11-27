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

/**
 * "As this enters, note the most prevalent creature type in [a library]" (Favored Enemy). An opponent's library is the
 * opponent chosen as it enters ({@code ChooseOpponentOnEnterEffect}).
 */
@Component
@RequiredArgsConstructor
public class NoteMostPrevalentCreatureTypeOnEnterEffectHandler implements EntryReplacementHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoteMostPrevalentCreatureTypeOnEnterEffect.class;
    }

    /**
     * Notes the type from the opponent chosen as {@code enteringPermanent} entered (its as-enters opponent choice),
     * once that choice is made: with two or more opponents the note waits for it.
     */
    public void applyAfterOpponentChoice(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        if (enteringPermanent.getChosenSubtype() != null || enteringPermanent.getChosenPlayerIds().isEmpty()) {
            return;
        }
        for (CardEffect effect : enteringPermanent.getCard().getStaticEffects()) {
            if (effect instanceof NoteMostPrevalentCreatureTypeOnEnterEffect note && note.opponentLibrary()) {
                apply(gameData, controllerId, enteringPermanent, effect);
            }
        }
    }

    /**
     * "An opponent's library": the opponent chosen as the permanent enters, or its controller's one opponent; with
     * two or more and none chosen yet, {@code null} (the note is made by {@link #applyAfterOpponentChoice}).
     */
    private static UUID notedOpponent(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        if (!enteringPermanent.getChosenPlayerIds().isEmpty()) {
            return enteringPermanent.getChosenPlayerIds().getFirst();
        }
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId) && gameData.playerIds.contains(playerId))
                .toList();
        return opponents.size() == 1 ? opponents.getFirst() : null;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        NoteMostPrevalentCreatureTypeOnEnterEffect noteEffect =
                (NoteMostPrevalentCreatureTypeOnEnterEffect) effect;
        UUID libraryOwnerId = noteEffect.opponentLibrary()
                ? notedOpponent(gameData, controllerId, enteringPermanent)
                : controllerId;
        if (libraryOwnerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(libraryOwnerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        EnumMap<CardSubtype, Integer> counts = new EnumMap<>(CardSubtype.class);
        for (Card card : library) {
            if (!card.hasType(CardType.CREATURE) && !card.hasType(CardType.KINDRED)) {
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
