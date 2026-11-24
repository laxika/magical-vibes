package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Records perpetual type, subtype, and activated-ability grants on a card identity; a grant to the cards a subject
 * names is logged as {@link PerpetualChangeLog} says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantCardCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantCardCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyGrantCardCharacteristicsEffect perpetual =
                (PerpetuallyGrantCardCharacteristicsEffect) effect;
        if (perpetual.cards() != null) {
            var located = subjectResolutionService.locatedCards(gameData, entry, perpetual, perpetual.cards());
            located.forEach(card -> grant(gameData, card.card().getId(), perpetual));
            PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, perpetual,
                    perpetual.cards(), located, change(perpetual));
            return;
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        Card card = source == null || source.getOriginalCard() == null
                ? entry.getCard() : source.getOriginalCard();
        if (card == null) {
            return;
        }
        grant(gameData, card.getId(), perpetual);
    }

    /** " perpetually becomes an artifact in addition to its other types.", " perpetually gains an ability.". */
    private static String change(PerpetuallyGrantCardCharacteristicsEffect perpetual) {
        List<String> words = new ArrayList<>();
        perpetual.subtypes().stream().map(subtype -> subtype.getDisplayName()).sorted().forEach(words::add);
        perpetual.cardTypes().stream().map(type -> type.getDisplayName().toLowerCase(Locale.ROOT)).sorted()
                .forEach(words::add);
        if (words.isEmpty()) {
            return " perpetually gains an ability.";
        }
        String becomes = String.join(" ", words);
        String article = "aeiouAEIOU".indexOf(becomes.charAt(0)) >= 0 ? "an " : "a ";
        return " perpetually becomes " + article + becomes
                + (perpetual.replacesCreatureTypes() ? "" : " in addition to its other types")
                + (perpetual.activatedAbilities().isEmpty() ? "" : " and gains an ability") + ".";
    }

    private void grant(GameData gameData, UUID cardId, PerpetuallyGrantCardCharacteristicsEffect perpetual) {
        mergeCardTypes(gameData, cardId, perpetual.cardTypes());
        if (perpetual.replacesCreatureTypes()) {
            gameData.perpetualCreatureTypeReplacedCardIds.add(cardId);
            gameData.perpetualCardSubtypes.computeIfPresent(cardId, (ignored, existing) -> {
                Set<CardSubtype> kept = EnumSet.noneOf(CardSubtype.class);
                existing.stream().filter(subtype -> !StaticEffectSupport.isCreatureSubtype(subtype))
                        .forEach(kept::add);
                return Set.copyOf(kept);
            });
        }
        mergeSubtypes(gameData, cardId, perpetual.subtypes());
        perpetual.activatedAbilities().forEach(ability -> gameData.grantPerpetualAbilityOnce(cardId, ability));
    }

    private void mergeCardTypes(GameData gameData, UUID cardId, Set<CardType> types) {
        gameData.perpetualCardTypes.merge(cardId, Set.copyOf(types), (existing, added) -> {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(existing);
            merged.addAll(added);
            return Set.copyOf(merged);
        });
    }

    private void mergeSubtypes(GameData gameData, UUID cardId, Set<CardSubtype> subtypes) {
        gameData.perpetualCardSubtypes.merge(cardId, Set.copyOf(subtypes), (existing, added) -> {
            Set<CardSubtype> merged = EnumSet.noneOf(CardSubtype.class);
            merged.addAll(existing);
            merged.addAll(added);
            return Set.copyOf(merged);
        });
    }
}
