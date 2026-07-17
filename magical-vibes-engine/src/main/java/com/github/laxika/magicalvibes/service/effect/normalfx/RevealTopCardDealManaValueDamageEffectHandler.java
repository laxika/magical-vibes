package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.List;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardDealManaValueDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardDealManaValueDamageEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetPlayerId)) return;

        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetPlayerName + "'s library is empty."));
            return;
        }

        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetPlayerName + " reveals " + topCard.getName() + " (mana value " + manaValue + ") from the top of their library."));

        if (manaValue > 0 && !gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);

            if (e.damageTargetPlayer()) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetPlayerId, damage);
            }

            if (e.damageTargetCreatures()) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
                if (battlefield != null) {
                    Predicate<Permanent> creatureFilter = p -> gameQueryService.isCreature(gameData, p);
                    damageSupport.damageFilteredCreatures(gameData, entry, damage, battlefield, creatureFilter);
                }
            }

            gameOutcomeService.checkWinCondition(gameData);
        }

        if (e.returnToHandIfLand() && topCard.hasType(CardType.LAND)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("A land card was revealed — " + cardName + " is returned to its owner's hand."));
            entry.setReturnToHandAfterResolving(true);
        }
    
    }
}
