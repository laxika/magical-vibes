package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCreaturesFromGraveyardAndCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesFromGraveyardAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCreaturesFromGraveyardAndCreateTokensEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        List<UUID> cardIdsToExile;
        if (e.targetPlayerGraveyard()) {
            // Necromancer's Covenant: exile all creature cards from the single targeted player's graveyard.
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(entry.getTargetId(), List.of());
            cardIdsToExile = graveyard.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .map(Card::getId)
                    .toList();
        } else {
            // Midnight Ritual / Hour of Eternity: exile the individually targeted creature cards.
            cardIdsToExile = entry.getTargetCardIds();
        }

        List<Card> exiledCards = new ArrayList<>();
        for (UUID cardId : cardIdsToExile) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " exiles " , card, " from graveyard."));
                exiledCards.add(card);
            }
        }

        // Hour of Eternity: create a token that's a copy of each exiled card, except it's a 4/4 black Zombie.
        if (e.copyExiledCards()) {
            List<CardSubtype> addedSubtypes = e.addedSubtype() != null ? List.of(e.addedSubtype()) : List.of();
            for (Card exiledCard : exiledCards) {
                graveyardReturnSupport.createTokenCopyFromCard(gameData, entry, exiledCard, addedSubtypes,
                        false, false, e.colorOverride(), e.powerOverride(), e.toughnessOverride());
            }
            return;
        }

        int tokensToCreate = exiledCards.size();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        int totalTokens = tokensToCreate * tokenMultiplier;
        for (int i = 0; i < totalTokens; i++) {
            Card tokenCard = new Card();
            tokenCard.setName("Zombie");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.BLACK);
            tokenCard.setPower(2);
            tokenCard.setToughness(2);
            tokenCard.setSubtypes(List.of(CardSubtype.ZOMBIE));

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String tokenLog = "A 2/2 Zombie creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(tokenLog));

            graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, tokenPermanent, tokenCard);

            log.info("Game {} - Zombie token created for player {}", gameData.id, controllerId);
        }
    }
}
