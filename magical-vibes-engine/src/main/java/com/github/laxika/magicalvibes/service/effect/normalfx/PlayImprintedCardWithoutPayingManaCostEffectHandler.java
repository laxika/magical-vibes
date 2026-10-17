package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolution of a Hideaway land's "you may play the exiled card without paying its mana cost"
 * ability (e.g. Howltooth Hollow). Offers the controller a may-choice to play the source
 * permanent's imprinted (face-down exiled) card; the actual play is carried out by
 * {@link com.github.laxika.magicalvibes.service.input.MayCastHandlerService#handlePlayImprintedCardChoice}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayImprintedCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayImprintedCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> exiledCards = new ArrayList<>();
        Card imprintedCard = gameData.getImprintedCard(entry.getCard());
        if (imprintedCard != null && gameData.findExiledCard(imprintedCard.getId()) != null) {
            exiledCards.add(imprintedCard);
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (sourcePermanentId.equals(exiled.sourcePermanentId()) && exiled.faceDown()
                        && exiledCards.stream().noneMatch(card -> card.getId().equals(exiled.card().getId()))) {
                    exiledCards.add(exiled.card());
                }
            }
        }

        if (exiledCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no exiled card to play."));
            return;
        }

        for (Card exiledCard : exiledCards.reversed()) {
            String prompt = "Play " + exiledCard.getName() + " without paying its mana cost?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    exiledCard,
                    controllerId,
                    List.of(effect),
                    prompt,
                    exiledCard.getId(),
                    null,
                    sourcePermanentId
            ));
        }
    }
}
