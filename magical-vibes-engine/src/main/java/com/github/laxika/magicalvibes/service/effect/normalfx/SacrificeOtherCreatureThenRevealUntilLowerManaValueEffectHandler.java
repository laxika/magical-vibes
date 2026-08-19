package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureThenRevealUntilLowerManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeOtherCreatureThenRevealUntilLowerManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureThenRevealUntilLowerManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (SacrificeOtherCreatureThenRevealUntilLowerManaValueEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();
        List<UUID> creatureIds = destructionSupport.collectCreatureIds(
                gameData, controllerId, permanent -> !permanent.getCard().getId().equals(sourceCardId));

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no other creature to sacrifice."));
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                resolveAfterChoice(gameData, entry, creature, typedEffect.predicate());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeOtherCreatureThenRevealUntilLowerManaValue(
                        controllerId, entry.getCard(), typedEffect.predicate()));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — Choose another creature to sacrifice.");
    }

    public void resolveAfterChoice(GameData gameData, StackEntry entry, Permanent creature,
                                   com.github.laxika.magicalvibes.model.filter.CardPredicate predicate) {
        int maximumManaValue = creature.getCard().getManaValue() - 1;
        destructionSupport.sacrificeAndLog(gameData, creature, entry.getControllerId());

        revealHandler.resolve(gameData, entry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAllOfPredicate(List.of(predicate,
                                new CardMaxManaValuePredicate(maximumManaValue))),
                        LibrarySearchDestination.BATTLEFIELD));
    }

    public void resolveAfterChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                   Permanent creature,
                                   com.github.laxika.magicalvibes.model.filter.CardPredicate predicate) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of());
        resolveAfterChoice(gameData, entry, creature, predicate);
    }
}
