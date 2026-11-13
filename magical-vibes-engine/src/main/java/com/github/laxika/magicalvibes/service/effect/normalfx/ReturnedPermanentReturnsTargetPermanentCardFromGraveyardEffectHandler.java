package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            returnedCardId = entry.getTargetCardIds().getFirst();
        }
        if (returnedCardId == null) {
            return;
        }

        Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
        if (returnedPermanent == null
                || gameQueryService.areCreatureETBTriggersSuppressed(gameData, returnedPermanent.getCard())) {
            return;
        }

        int maximumManaValue = returnedPermanent.getCard().getManaValue();
        CardAllOfPredicate permanentWithinManaValue = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardMaxManaValuePredicate(maximumManaValue)));
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(permanentWithinManaValue)
                .targetGraveyard(true)
                .upTo(true)
                .build();

        // The returned permanent is the reflexive ability's source permanent (CR 603.12); its graveyard target is
        // chosen as it goes on the stack.
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, List.of(returnEffect),
                entry.getCard().getName() + "'s reflexive ability", returnedPermanent.getId());
        record.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
