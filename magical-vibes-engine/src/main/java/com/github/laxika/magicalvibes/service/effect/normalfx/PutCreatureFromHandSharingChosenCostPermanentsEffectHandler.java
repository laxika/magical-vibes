package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandSharingChosenCostPermanentsEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class PutCreatureFromHandSharingChosenCostPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCreatureFromHandSharingChosenCostPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> chosenPermanents = chosenPermanents(gameData, entry);
        if (chosenPermanents.size() != 2) {
            return;
        }

        Predicate<Card> sharesWithBoth = card -> chosenPermanents.stream()
                .allMatch(permanent -> gameQueryService.shareCreatureType(gameData, permanent, card));
        PutCardToBattlefieldEffect putCreature = new PutCardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), "creature");
        playerInteractionSupport.applyPutCardToBattlefield(
                gameData, entry.getControllerId(), putCreature, entry.getXValue(), null,
                entry.getCard() == null ? null : entry.getCard().getId(), sharesWithBoth);
    }

    private List<Permanent> chosenPermanents(GameData gameData, StackEntry entry) {
        List<UUID> chosenIds = entry.getChosenCostPermanentIds();
        List<Permanent> snapshots = entry.getChosenCostPermanentSnapshots();
        List<Permanent> result = new ArrayList<>();
        for (UUID chosenId : chosenIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
            if (chosen == null) {
                chosen = snapshots.stream()
                        .filter(snapshot -> chosenId.equals(snapshot.getId()))
                        .findFirst()
                        .orElse(null);
            }
            if (chosen != null) {
                result.add(chosen);
            }
        }
        return result;
    }
}
