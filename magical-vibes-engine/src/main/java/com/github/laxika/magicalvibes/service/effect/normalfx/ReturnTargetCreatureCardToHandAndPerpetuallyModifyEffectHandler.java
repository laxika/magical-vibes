package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GrantedAlternateCast;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
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
            // Changes to the physical card, kept in the perpetual stores, not to its copiable values (CR 707.2):
            // "becomes a Spirit" replaces its creature types (CR 205.1a).
            UUID cardId = targetCard.getId();
            PerpetuallyGrantCardCharacteristicsEffectHandler.grant(gameData, cardId,
                    PerpetuallyGrantCardCharacteristicsEffect.becomingCreatureTypes(Set.of(modification.subtype())));
            gameData.perpetualCardBasePowerToughness.put(cardId, new GameData.PerpetualBasePowerToughness(
                    modification.power(), modification.toughness(), gameData.nextTimestamp()));
            gameData.grantPerpetualAlternateCast(cardId, new GrantedAlternateCast(new AlternateHandCast(List.of(
                    new ManaCastingCost(modification.alternateManaCost()))), false));
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
