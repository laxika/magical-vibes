package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Records perpetual type, subtype, and activated-ability grants on a card identity. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantCardCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantCardCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyGrantCardCharacteristicsEffect perpetual =
                (PerpetuallyGrantCardCharacteristicsEffect) effect;
        if (perpetual.cards() != null) {
            subjectResolutionService.cards(gameData, entry, perpetual, perpetual.cards())
                    .forEach(card -> grant(gameData, card.getId(), perpetual));
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
