package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
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
