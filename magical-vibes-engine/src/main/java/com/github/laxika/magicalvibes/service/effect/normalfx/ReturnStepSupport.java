package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** What a return of a permanent to a hand records on its {@code StepKind.RETURN} step. */
public final class ReturnStepSupport {

    private ReturnStepSupport() {
    }

    /** {@code permanent} as it is before it leaves, which is what a later step reads of it (CR 608.2h). */
    public static LastKnownCardStats lastKnown(GameData gameData, GameQueryService gameQueryService,
                                               Permanent permanent) {
        return new LastKnownCardStats(permanent.getId(), gameQueryService.getEffectivePower(gameData, permanent),
                gameQueryService.getEffectiveToughness(gameData, permanent),
                gameQueryService.getPermanentManaValue(permanent),
                gameQueryService.getEffectiveName(gameData, permanent),
                gameQueryService.findPermanentController(gameData, permanent.getId()));
    }

    /** {@code permanent}, which left for a hand: whose hand, the cards it became there, and as it last existed. */
    public static StepOutcome returned(GameData gameData, Permanent permanent, List<Card> leaving,
                                       LastKnownCardStats known) {
        List<MovedCard> moved = new ArrayList<>();
        Set<UUID> handOwners = new LinkedHashSet<>();
        for (Card card : leaving) {
            gameData.playerHands.forEach((playerId, hand) -> {
                if (hand.stream().anyMatch(held -> held.getId().equals(card.getId()))) {
                    handOwners.add(playerId);
                    if (!card.isToken()) {
                        moved.add(new MovedCard(permanent.getId(), card.getId(), Zone.HAND, playerId));
                    }
                }
            });
        }
        StepOutcome outcome = StepOutcome.ofObjects(List.of(permanent.getId()), List.copyOf(handOwners), moved);
        return known == null ? outcome : outcome.withLastKnownPermanents(List.of(known));
    }
}
