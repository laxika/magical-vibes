package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPutSelectedCardOntoBattlefieldEffectHandler implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPutSelectedCardOntoBattlefieldEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            List<Card> hand = gameData.playerHands.get(player.getId());
            Card selectedCard = hand == null ? null : hand.stream()
                    .filter(card -> card.getId().equals(ability.targetCardId()))
                    .findFirst()
                    .orElse(null);
            if (selectedCard != null) {
                hand.remove(selectedCard);
                Permanent permanent = new Permanent(selectedCard, Zone.LIBRARY);
                permanent.getGrantedKeywords().add(Keyword.HASTE);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), permanent);
                if (selectedCard.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(
                            gameData, player.getId(), selectedCard, null, false);
                }
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " puts ").card(selectedCard)
                        .text(" onto the battlefield with haste.").build());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
