package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAndCloakDisguisedCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileAndCloakDisguisedCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAndCloakDisguisedCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> validIds = battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> gameQueryService.hasKeyword(
                        gameData, permanent, com.github.laxika.magicalvibes.model.Keyword.DISGUISE))
                .map(Permanent::getId)
                .toList();
        if (validIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData, controllerId, validIds, validIds.size(),
                new MultiPermanentChoiceContext.RecloakDisguisedCreatures(entry),
                entry.getCard().getName()
                        + " — Choose any number of face-up creatures you control with disguise to cloak.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.RecloakDisguisedCreatures context) {
        List<CloakedCard> cards = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }
            List<Card> leavingCards = new ArrayList<>(permanent.cardsLeavingBattlefield());
            if (!permanentRemovalService.removePermanentToExileFaceDown(gameData, permanent)) {
                continue;
            }
            for (Card card : leavingCards) {
                ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
                if (exiled != null) {
                    cards.add(new CloakedCard(exiled.card(), exiled.ownerId()));
                }
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        Collections.shuffle(cards);

        Set<com.github.laxika.magicalvibes.model.CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (CloakedCard card : cards) {
            if (!gameData.removeFromExile(card.card().getId())) {
                continue;
            }
            if (card.card().isToken()) {
                continue;
            }
            Permanent cloaked = new Permanent(card.card());
            cloaked.setFaceDownAsCloaked();
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, card.ownerId(), cloaked, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(cloaked);
            battlefieldEntryService.processFaceDownCreatureETBTriggers(
                    gameData, card.ownerId(), card.card());
        }

        if (!cards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(context.resolvingEntry().getControllerId())
                            + " exiles and cloaks " + cards.size() + " creature(s)."));
        }
    }

    private record CloakedCard(Card card, UUID ownerId) {
    }
}
