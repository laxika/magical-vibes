package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureThenRevealUntilHigherManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCreatureThenRevealUntilHigherManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureThenRevealUntilHigherManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (!entry.getControllerId().equals(targetControllerId)) {
            return;
        }

        int targetManaValue = target.getCard().getManaValue();
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));

        StackEntry revealEntry = new StackEntry(entry);
        revealEntry.setControllerId(targetControllerId);
        revealHandler.resolve(gameData, revealEntry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMinManaValuePredicate(targetManaValue + 1))),
                        LibrarySearchDestination.BATTLEFIELD));
    }
}
