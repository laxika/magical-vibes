package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * What a land-tap triggered mana ability reads from its stack entry as it resolves: the tapped land (the entry's
 * triggering permanent), the types of mana it produced and the player who tapped it.
 */
public final class TappedLandManaSupport {

    private TappedLandManaSupport() {
    }

    /**
     * The tapped land as it is on the battlefield, or as it last existed there once its mana ability made it leave
     * (CR 608.2h); {@code null} when the entry names none.
     */
    public static Permanent tappedLand(GameData gameData, GameQueryService gameQueryService, StackEntry entry) {
        if (entry.getTriggeringPermanentId() != null) {
            Permanent live = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            if (live != null) {
                return live;
            }
        }
        return entry.getTriggeringPermanentSnapshot();
    }

    /**
     * The types of mana the tap produced: the entry's produced mana colors, or, when the tap reported none, the colors
     * the land's basic land types make it add (CR 305.6).
     */
    public static Set<ManaColor> producedManaTypes(StackEntry entry, Permanent tappedLand) {
        Set<ManaColor> produced = new LinkedHashSet<>(entry.getProducedManaColors());
        if (produced.isEmpty() && tappedLand != null) {
            produced.addAll(intrinsicManaColors(tappedLand));
        }
        return produced;
    }

    /**
     * The ability's source as it is on the battlefield, or as it was when the ability triggered (the entry's source
     * snapshot); {@code null} for a source with no permanent (a face-up plane, a player's granted ability).
     */
    public static Permanent sourcePermanent(GameData gameData, GameQueryService gameQueryService, StackEntry entry) {
        Permanent live = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return live != null ? live : entry.getSourcePermanentSnapshot();
    }

    /**
     * Adds {@code amount} mana of {@code color} to {@code recipientId}'s pool for the triggered mana ability of
     * {@code entry}, logging "adds N additional [color] mana"; nothing when the player has no pool.
     */
    public static void addAdditionalMana(GameData gameData, GameLogService gameLogService, StackEntry entry,
                                         UUID recipientId, ManaColor color, int amount) {
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null || amount <= 0) {
            return;
        }
        pool.add(color, amount);
        gameLogService.append(gameData, entry.getCard() == null
                ? GameLog.text(gameData.playerIdToName.get(recipientId) + " adds " + amount + " additional "
                        + color.name().toLowerCase() + " mana.")
                : GameLog.cardThen(entry.getCard(), " triggers — " + gameData.playerIdToName.get(recipientId)
                        + " adds " + amount + " additional " + color.name().toLowerCase() + " mana."));
    }

    /**
     * "Its controller adds": the controller of the entry's triggering permanent while it is on the battlefield, else the
     * player who tapped it (the entry's triggering player), else the entry's controller.
     */
    public static UUID tappedPermanentController(GameData gameData, GameQueryService gameQueryService,
                                                 StackEntry entry) {
        UUID controllerId = entry.getTriggeringPermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
        return controllerId != null ? controllerId : manaRecipient(entry);
    }

    /** "That player adds": the entry's triggering player (who tapped the land), else its controller. */
    public static UUID manaRecipient(StackEntry entry) {
        return entry.getTriggeringPlayerId() != null ? entry.getTriggeringPlayerId() : entry.getControllerId();
    }

    /** The colors the land card's basic land types make it add (CR 305.6), for a tap that reported none. */
    public static List<ManaColor> intrinsicManaColors(Permanent land) {
        return ManaAbilities.intrinsic(land.getCard()).stream()
                .filter(ActivatedAbility::isRequiresTap)
                .flatMap(ability -> ability.getEffects().stream())
                .filter(AwardManaEffect.class::isInstance)
                .map(effect -> ((AwardManaEffect) effect).color())
                .toList();
    }
}
