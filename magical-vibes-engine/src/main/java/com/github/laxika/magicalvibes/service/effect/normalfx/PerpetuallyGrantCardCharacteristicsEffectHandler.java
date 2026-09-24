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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Records perpetual type, subtype, and activated-ability grants on a card identity. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantCardCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantCardCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyGrantCardCharacteristicsEffect perpetual =
                (PerpetuallyGrantCardCharacteristicsEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card card = source == null || source.getOriginalCard() == null
                ? entry.getCard() : source.getOriginalCard();
        if (card == null) {
            return;
        }

        UUID cardId = card.getId();
        mergeCardTypes(gameData, cardId, perpetual.cardTypes());
        mergeSubtypes(gameData, cardId, perpetual.subtypes());
        gameData.perpetualActivatedAbilities.compute(cardId, (ignored, existing) -> {
            List<com.github.laxika.magicalvibes.model.ActivatedAbility> updated =
                    new ArrayList<>(existing == null ? List.of() : existing);
            for (var ability : perpetual.activatedAbilities()) {
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
            }
            return List.copyOf(updated);
        });

        if (source != null) {
            source.getPersistentGrantedCardTypes().addAll(perpetual.cardTypes());
            perpetual.subtypes().forEach(subtype -> {
                if (!source.getGrantedSubtypes().contains(subtype)) {
                    source.getGrantedSubtypes().add(subtype);
                }
            });
            perpetual.activatedAbilities().forEach(ability -> {
                if (!source.getPersistentGrantedActivatedAbilities().contains(ability)) {
                    source.getPersistentGrantedActivatedAbilities().add(ability);
                }
            });
        }
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
