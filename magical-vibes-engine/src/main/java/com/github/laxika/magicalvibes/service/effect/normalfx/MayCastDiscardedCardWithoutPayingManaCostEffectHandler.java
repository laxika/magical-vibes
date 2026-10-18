package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastDiscardedCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastDiscardedCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        // A replacement effect (Leyline of the Void) may have exiled the discarded card instead; the
        // effect can still find it there, since the discard moved it to a public zone (CR 400.7j).
        ExiledCardEntry exiledEntry = gameData.findExiledCard(discardedCardId);
        Card discardedCard;
        if (exiledEntry != null) {
            if (!entry.getControllerId().equals(exiledEntry.ownerId())) {
                return;
            }
            discardedCard = exiledEntry.card();
        } else {
            discardedCard = gameQueryService.findCardInGraveyardById(gameData, discardedCardId);
            UUID graveyardOwnerId = discardedCard == null
                    ? null : gameQueryService.findGraveyardOwnerById(gameData, discardedCardId);
            if (discardedCard == null || !entry.getControllerId().equals(graveyardOwnerId)) {
                return;
            }
            long expectedEntryVersion = entry.getTriggeringCardGraveyardEntryVersion();
            if (expectedEntryVersion >= 0
                    && gameData.graveyardEntryVersion(discardedCardId) != expectedEntryVersion) {
                return;
            }
        }
        if (discardedCard.hasType(CardType.LAND)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getCounterCount(CounterType.CHORUS) < 4) {
            return;
        }

        String prompt = entry.getCard().getName() + " — Cast " + discardedCard.getName()
                + " without paying its mana cost?";
        if (exiledEntry != null) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    discardedCard,
                    entry.getControllerId(),
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    prompt,
                    discardedCard.getId()));
            return;
        }
        PlayTargetCardFromGraveyardWithoutPayingManaCostEffect castEffect =
                new PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)));
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                discardedCard,
                entry.getControllerId(),
                List.of(castEffect),
                prompt));
    }
}
