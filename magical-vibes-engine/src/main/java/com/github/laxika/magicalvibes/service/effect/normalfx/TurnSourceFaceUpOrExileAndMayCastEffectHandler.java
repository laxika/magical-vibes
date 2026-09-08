package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpOrExileAndMayCastEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnSourceFaceUpOrExileAndMayCastEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnSourceFaceUpOrExileAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isFaceDown()) {
            return;
        }

        Card originalCard = source.getOriginalCard();
        boolean canTurnFaceUp = gameQueryService.isCreature(gameData, source)
                && !gameQueryService.isTurnFaceUpPrevented(gameData, source)
                && !originalCard.hasType(CardType.INSTANT)
                && !originalCard.hasType(CardType.SORCERY);
        if (canTurnFaceUp) {
            gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, source);
            return;
        }

        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return;
        }

        if (!originalCard.hasType(CardType.LAND)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + originalCard.getName() + " without paying its mana cost?",
                    originalCard.getId()
            ));
        }
    }
}
