package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The log of a perpetual change to the cards one instruction names. The public log says only what the card's text makes
 * public (CR 400.2): a card in a public zone is named, one line each, but the cards in a hand or library get one line
 * per owner and zone, "Each affected card in Alice's hand perpetually gains an ability.", which names no card and gives
 * no count. An instruction over the cards in a hand or library ({@link CardSubject#inHiddenZone()}) logs that line also
 * when no card there matches, so the line doesn't tell whether one did. An instruction over every card a player owns
 * ({@link CardSubject.Owned}) is one line per owner, "Each affected card Alice owns perpetually gets +1/+1.", which
 * names no card even where some are public: it may name hidden and face-down ones too.
 */
final class PerpetualChangeLog {

    private PerpetualChangeLog() {
    }

    /**
     * Logs {@code change} (" perpetually gains an ability.") for the cards {@code subject} named.
     *
     * @param located the cards the change was made to, each where it is
     */
    static void log(GameLogService gameLogService, SubjectResolutionService subjectResolutionService,
                    GameData gameData, StackEntry entry, CardEffect effect, CardSubject subject,
                    List<LocatedCard> located, String change) {
        if (subject instanceof CardSubject.Owned owned) {
            for (UUID owner : subjectResolutionService.players(gameData, entry, effect, owned.owners())) {
                gameLogService.append(gameData, GameLog.text("Each affected card "
                        + gameData.playerIdToName.get(owner) + " owns" + change));
            }
            return;
        }
        Set<HiddenZone> hiddenZones = new LinkedHashSet<>();
        addHiddenZones(subjectResolutionService, gameData, entry, effect, subject, hiddenZones);
        for (LocatedCard card : located) {
            if (isHidden(card.zone())) {
                hiddenZones.add(new HiddenZone(card.ownerId(), card.zone()));
            } else if (isFaceDownObject(gameData, card)) {
                // A face-down permanent or spell's card is not named (CR 708.2, 708.5).
                gameLogService.append(gameData, GameLog.text((card.zone() == Zone.STACK
                        ? "A face-down spell" : "A face-down permanent") + change));
            } else {
                gameLogService.append(gameData, GameLog.cardThenUnlessHidden(gameData, card.card(), change));
            }
        }
        for (HiddenZone zone : hiddenZones) {
            gameLogService.append(gameData, GameLog.text("Each affected card in "
                    + gameData.playerIdToName.get(zone.ownerId()) + "'s "
                    + (zone.zone() == Zone.HAND ? "hand" : "library") + change));
        }
    }

    private static boolean isFaceDownObject(GameData gameData, LocatedCard card) {
        UUID cardId = card.card().getId();
        if (card.zone() == Zone.BATTLEFIELD) {
            return gameData.playerBattlefields.values().stream().flatMap(List::stream)
                    .anyMatch(permanent -> permanent.isFaceDown() && (permanent.getCard().getId().equals(cardId)
                            || permanent.getOriginalCard() != null
                            && permanent.getOriginalCard().getId().equals(cardId)));
        }
        return card.zone() == Zone.STACK && gameData.stack.stream()
                .anyMatch(spell -> spell.isCastFaceDown() && spell.getPhysicalCard() != null
                        && spell.getPhysicalCard().getId().equals(cardId));
    }

    private static boolean isHidden(Zone zone) {
        return zone == Zone.HAND || zone == Zone.LIBRARY;
    }

    private static void addHiddenZones(SubjectResolutionService subjectResolutionService, GameData gameData,
                                       StackEntry entry, CardEffect effect, CardSubject subject,
                                       Set<HiddenZone> zones) {
        switch (subject) {
            case CardSubject.InHand in -> subjectResolutionService.players(gameData, entry, effect, in.owners())
                    .forEach(owner -> zones.add(new HiddenZone(owner, Zone.HAND)));
            case CardSubject.InLibrary in -> subjectResolutionService.players(gameData, entry, effect, in.owners())
                    .forEach(owner -> zones.add(new HiddenZone(owner, Zone.LIBRARY)));
            case CardSubject.TopmostInLibrary top ->
                    subjectResolutionService.players(gameData, entry, effect, top.owners())
                            .forEach(owner -> zones.add(new HiddenZone(owner, Zone.LIBRARY)));
            case CardSubject.Except except ->
                    addHiddenZones(subjectResolutionService, gameData, entry, effect, except.cards(), zones);
            default -> {
            }
        }
    }

    private record HiddenZone(UUID ownerId, Zone zone) {
    }
}
