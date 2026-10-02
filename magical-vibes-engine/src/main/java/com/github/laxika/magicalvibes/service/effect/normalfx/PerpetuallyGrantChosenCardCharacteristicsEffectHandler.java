package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantChosenCardCharacteristicsEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Records perpetual characteristic grants on the card selected by a library search. */
@Component
public class PerpetuallyGrantChosenCardCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantChosenCardCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantChosenCardCharacteristicsEffect) effect;
        if (entry.getChosenObjectCard() == null) {
            return;
        }

        UUID cardId = entry.getChosenObjectCard().getId();
        mergeCardTypes(gameData, cardId, grant.cardTypes());
        mergeSubtypes(gameData, cardId, grant.subtypes());
        gameData.perpetualActivatedAbilities.compute(cardId, (ignored, existing) -> {
            List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
            grant.activatedAbilities().forEach(ability -> {
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
            });
            return List.copyOf(updated);
        });
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
