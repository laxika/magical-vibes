package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Chooses legal attachments before putting a batch of cards onto the battlefield together. */
@Component
@RequiredArgsConstructor
public class BattlefieldEntryBatchSupport {
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    public void begin(GameData gameData, List<BattlefieldEntryCard> cards) {
        continueChoices(gameData, cards, new ArrayList<>());
    }

    public void completeChoice(GameData gameData, UUID attachmentId,
                               PermanentChoiceContext.AuraEntryBatchChoice choice) {
        List<BattlefieldEntryCard> ready = new ArrayList<>(choice.ready());
        ready.add(choice.remaining().getFirst().withAttachment(attachmentId));
        continueChoices(gameData, choice.remaining().subList(1, choice.remaining().size()), ready);
    }

    private void continueChoices(GameData gameData, List<BattlefieldEntryCard> remaining,
                                 List<BattlefieldEntryCard> ready) {
        for (int i = 0; i < remaining.size(); i++) {
            BattlefieldEntryCard candidate = remaining.get(i);
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, candidate.card(), candidate.origin())) {
                continue;
            }
            if (!candidate.card().isAura()) {
                ready.add(candidate);
                continue;
            }
            List<UUID> hosts = new ArrayList<>();
            if (!candidate.card().isEnchantPlayer()) {
                gameData.forEachPermanent((controllerId, permanent) -> {
                    if (!gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                            && auraAttachmentService.canEnchant(gameData, candidate.card(), candidate.controllerId(), permanent)) {
                        hosts.add(permanent.getId());
                    }
                });
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, candidate.card(), candidate.controllerId(), playerId)) {
                    hosts.add(playerId);
                }
            }
            if (hosts.isEmpty()) continue;
            if (hosts.size() == 1) {
                ready.add(candidate.withAttachment(hosts.getFirst()));
            } else {
                playerInputService.beginPermanentChoice(gameData, candidate.controllerId(), hosts,
                        new PermanentChoiceContext.AuraEntryBatchChoice(remaining.subList(i, remaining.size()), ready),
                        "Choose what " + candidate.card().getName() + " will enchant.");
                return;
            }
        }
        placeCards(gameData, ready);
    }

    private void placeCards(GameData gameData, List<BattlefieldEntryCard> cards) {
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (BattlefieldEntryCard candidate : cards) {
                List<Card> zone = candidate.origin() == Zone.GRAVEYARD
                        ? gameData.playerGraveyards.get(candidate.zoneOwnerId())
                        : gameData.playerHands.get(candidate.zoneOwnerId());
                if (zone == null || !zone.remove(candidate.card())) continue;
                if (candidate.origin() == Zone.GRAVEYARD) {
                    graveyardService.notifyCardsLeftGraveyard(gameData, candidate.zoneOwnerId(), candidate.card());
                }
                Permanent permanent = new Permanent(candidate.card(), candidate.origin());
                permanent.setAttachedTo(candidate.attachmentId());
                if (candidate.origin() == Zone.GRAVEYARD) {
                    permanent.setEnteredFromGraveyardOwnerId(candidate.zoneOwnerId());
                }
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, candidate.controllerId(), permanent,
                        enterTappedTypes, List.copyOf(entered));
                entered.add(permanent);
                gameLogService.append(gameData, GameLog.cardThen(candidate.card(), " enters the battlefield."));
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Permanent permanent : entered) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null) {
                battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, permanent.getCard(), null, false);
            }
        }
    }
}
