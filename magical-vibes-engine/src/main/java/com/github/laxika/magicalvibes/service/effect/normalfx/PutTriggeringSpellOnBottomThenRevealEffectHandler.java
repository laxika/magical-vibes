package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringSpellOnBottomThenRevealEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Neera's spell-bottoming and library-reveal sequence. */
@Component
public class PutTriggeringSpellOnBottomThenRevealEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;
    private final TriggerCollectionService triggerCollectionService;
    private final RevealUntilCardPredicateMayCastWithoutPayingManaEffectHandler revealHandler;

    public PutTriggeringSpellOnBottomThenRevealEffectHandler(
            GameLogService gameLogService,
            StateTriggerService stateTriggerService,
            @Lazy TriggerCollectionService triggerCollectionService,
            RevealUntilCardPredicateMayCastWithoutPayingManaEffectHandler revealHandler) {
        this.gameLogService = gameLogService;
        this.stateTriggerService = stateTriggerService;
        this.triggerCollectionService = triggerCollectionService;
        this.revealHandler = revealHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTriggeringSpellOnBottomThenRevealEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> candidate.getCard() != null)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .findFirst()
                .orElse(null);
        if (spell == null) {
            return;
        }

        Card physicalCard = spell.getPhysicalCard();
        UUID ownerId = spell.getOwnerId();
        if (physicalCard == null || ownerId == null || gameData.playerDecks.get(ownerId) == null) {
            return;
        }

        gameData.stack.remove(spell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);
        gameData.playerDecks.get(ownerId).add(physicalCard);
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, ownerId, 1);
        gameLogService.append(gameData, GameLog.cardThen(physicalCard,
                " is put on the bottom of its owner's library."));

        PutTriggeringSpellOnBottomThenRevealEffect typedEffect =
                (PutTriggeringSpellOnBottomThenRevealEffect) effect;
        revealHandler.resolve(gameData, entry,
                new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(typedEffect.predicate()));
    }
}
