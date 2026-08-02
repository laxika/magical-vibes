package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared steps of {@code MagmasaurUpkeepEffect}, used by the resolution handler and the
 * accept/decline handler.
 */
@Component
@RequiredArgsConstructor
public class MagmasaurUpkeepSupport {

    private static final PermanentNotPredicate WITHOUT_FLYING =
            new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final DestructionSupport destructionSupport;
    private final MassDamageEffectHandler massDamageEffectHandler;

    /** The +1/+1 counters currently on the source, or 0 if it has already left the battlefield. */
    public int counters(GameData gameData, UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source == null ? 0 : source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
    }

    /** "You may remove a +1/+1 counter from this creature." */
    public void removeCounter(GameData gameData, UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }
        int remaining = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - 1;
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, Math.max(0, remaining));
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " has a +1/+1 counter removed from it."));
    }

    /**
     * "Sacrifice this creature and it deals damage equal to the number of +1/+1 counters on it to
     * each creature without flying and each player." The counter count is read before the sacrifice
     * — once the creature is gone the damage uses that last known information (CR 608.2h).
     */
    public void applyPenalty(GameData gameData, UUID controllerId, UUID sourcePermanentId, Card sourceCard) {
        int damage = counters(gameData, sourcePermanentId);

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            destructionSupport.sacrificeAndLog(gameData, source, controllerId);
        }
        if (damage <= 0) {
            return;
        }

        MassDamageEffect massDamage = new MassDamageEffect(new Fixed(damage), true, false, WITHOUT_FLYING);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of(massDamage),
                null,
                sourcePermanentId);
        massDamageEffectHandler.resolve(gameData, entry, massDamage);
    }
}
