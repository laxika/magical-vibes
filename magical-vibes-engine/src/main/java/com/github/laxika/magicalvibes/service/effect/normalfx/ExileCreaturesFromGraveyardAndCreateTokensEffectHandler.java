package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
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

    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;
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

        List<UUID> graveyardOwners = e.targetPlayerGraveyard()
                ? List.of(entry.getTargetId())
                : e.graveyardScope().graveyardOwners(gameData.orderedPlayerIds, controllerId);
        List<Card> exiledCards = new ArrayList<>();
        for (UUID cardId : cardIdsToExile) {
            Card card = graveyardOwners.stream()
                    .flatMap(ownerId -> gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream())
                    .filter(candidate -> candidate.getId().equals(cardId) && candidate.hasType(CardType.CREATURE))
                    .findFirst().orElse(null);
            if (card != null) {
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
                gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles " , card, " from graveyard."));
                exiledCards.add(card);
            }
        }

        // Hour of Eternity: create a token that's a copy of each exiled card, except it's a 4/4 black Zombie
        // (no "in addition to its other types", so the copy's creature types are replaced).
        if (e.copyExiledCards()) {
            List<CardSubtype> addedSubtypes = e.addedSubtype() != null ? List.of(e.addedSubtype()) : List.of();
            // Hour of Eternity creates every token copy at once, so a copy carrying an
            // enters-with-counters or enters-as-copy ability must not apply it to its batch-mates.
            List<Permanent> copyBatch = new ArrayList<>();
            for (Card exiledCard : exiledCards) {
                graveyardReturnSupport.createTokenCopyFromCard(gameData, entry, exiledCard, addedSubtypes,
                        false, false, e.colorOverride(), e.powerOverride(), e.toughnessOverride(),
                        true, false, copyBatch, Set.of());
            }
            return;
        }

        // One token creation event for every Zombie, so the player orders its count replacements and
        // additions (CR 616.1); the creation may pause for that choice.
        if (!exiledCards.isEmpty()) {
            permanentControlSupport.applyCreateToken(gameData, controllerId,
                    CreateTokenEffect.blackZombie(exiledCards.size()), exiledCards.size(),
                    entry.getCard().getSetCode());
        }
    }
}
