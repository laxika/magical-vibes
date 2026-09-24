package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeRandomGraveyardPermanentFoodEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Resourceful Collector's perpetual graveyard Food conversion. */
@Component
public class PerpetuallyMakeRandomGraveyardPermanentFoodEffectHandler implements NormalEffectHandlerBean {

    private static final ActivatedAbility FOOD_ABILITY = new ActivatedAbility(
            false,
            "{2}",
            List.of(new ExileSelfCost(), new GainLifeEffect(3)),
            "{2}, Exile this artifact: You gain 3 life."
    );

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyMakeRandomGraveyardPermanentFoodEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        List<Card> candidates = graveyard.stream()
                .filter(this::isEligible)
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Card modifiedCard = selected.createRuntimeCopy();

        EnumSet<CardType> additionalTypes = modifiedCard.getAdditionalTypes().isEmpty()
                ? EnumSet.noneOf(CardType.class)
                : EnumSet.copyOf(modifiedCard.getAdditionalTypes());
        additionalTypes.add(CardType.ARTIFACT);
        modifiedCard.setAdditionalTypes(additionalTypes);

        List<CardSubtype> subtypes = new ArrayList<>(modifiedCard.getSubtypes());
        subtypes.add(CardSubtype.FOOD);
        modifiedCard.setSubtypes(List.copyOf(subtypes));
        modifiedCard.addActivatedAbility(FOOD_ABILITY);
        modifiedCard.freeze();

        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(selected.getId())) {
                graveyard.set(i, modifiedCard);
                break;
            }
        }
        gameData.graveyardPlayPermissions.put(modifiedCard.getId(), controllerId);
    }

    private boolean isEligible(Card card) {
        boolean permanent = card.getType().isPermanentType()
                || card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType);
        return permanent && !card.getSubtypes().contains(CardSubtype.FOOD);
    }
}
