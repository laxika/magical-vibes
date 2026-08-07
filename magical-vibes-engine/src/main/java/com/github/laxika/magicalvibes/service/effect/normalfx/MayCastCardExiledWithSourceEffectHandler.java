package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MayCastCardExiledWithSourceEffect} (Shell of the Last Kappa): the ability's
 * controller may cast one of the cards exiled with the source permanent without paying its mana
 * cost.
 *
 * <p>One offer is queued per exiled card, mirroring the Counterlash may-cast routing; the offers
 * are marked exclusive so accepting one withdraws the rest — only a single spell is cast. Lands are
 * not spells and are never offered.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) return;

        List<Card> exiled = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (exiled.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no cards exiled with it to cast."));
            return;
        }

        for (int i = exiled.size() - 1; i >= 0; i--) {
            Card card = exiled.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card, controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect(true)),
                    "Cast " + card.getName() + " without paying its mana cost?",
                    card.getId()
            ));
        }

        log.info("Game {} - {} offers a free cast of {} card(s) exiled with it",
                gameData.id, entry.getCard().getName(), exiled.size());
    }

    /**
     * The source is sacrificed as a cost, so it is already off the battlefield when the ability
     * resolves — the activated ability's stack entry still carries its permanent id. The
     * battlefield lookup is only a fallback for a source that is still around.
     */
    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) return entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) return permanent.getId();
        }
        return null;
    }
}
