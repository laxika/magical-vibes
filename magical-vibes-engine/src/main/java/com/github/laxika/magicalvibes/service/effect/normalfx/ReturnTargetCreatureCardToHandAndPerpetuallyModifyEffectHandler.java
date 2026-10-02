package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a targeted graveyard return while carrying the perpetual card changes onto the card. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var modification = (ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect) effect;
        UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard != null
                && graveyardOwnerId != null
                && graveyardOwnerId.equals(entry.getControllerId())
                && targetCard.hasType(CardType.CREATURE)) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            if (graveyard != null) {
                for (int i = 0; i < graveyard.size(); i++) {
                    if (!graveyard.get(i).getId().equals(targetCardId)) {
                        continue;
                    }

                    Card modifiedCard = targetCard.createRuntimeCopy();
                    List<CardSubtype> subtypes = new ArrayList<>(targetCard.getSubtypes());
                    if (!subtypes.contains(modification.subtype())) {
                        subtypes.add(modification.subtype());
                    }
                    modifiedCard.setSubtypes(List.copyOf(subtypes));
                    modifiedCard.setPower(modification.power());
                    modifiedCard.setToughness(modification.toughness());
                    modifiedCard.addCastingOption(new AlternateHandCast(List.of(
                            new ManaCastingCost(modification.alternateManaCost()))));
                    modifiedCard.freeze();
                    graveyard.set(i, modifiedCard);
                    break;
                }
            }
        }

        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        graveyardReturnSupport.resolvePreTargetedById(
                gameData, entry, returnEffect, entry.getControllerId(), entry.getCard().getId(), targetCardId);
    }
}
