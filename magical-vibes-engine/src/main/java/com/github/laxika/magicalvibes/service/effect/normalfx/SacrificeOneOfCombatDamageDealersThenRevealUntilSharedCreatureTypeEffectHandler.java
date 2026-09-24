package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithSourcePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> validIds = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> typedEffect.combatDamageDealerIds().contains(permanent.getId()))
                .map(Permanent::getId)
                .toList();

        if (validIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " controls no creature that dealt combat damage to sacrifice."));
            return;
        }

        if (validIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            if (creature != null) {
                resolveAfterChoice(gameData, entry.getCard(), controllerId, creature);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureType(
                        controllerId, entry.getCard()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose one of them to sacrifice.");
    }

    public void resolveAfterChoice(GameData gameData, Card sourceCard,
                                   UUID controllerId, Permanent creature) {
        if (!gameData.playerBattlefields.getOrDefault(controllerId, List.of()).contains(creature)) {
            return;
        }

        Permanent sacrificedSnapshot = new Permanent(creature);
        destructionSupport.sacrificeAndLog(gameData, creature, controllerId);

        StackEntry continuation = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of(),
                0,
                creature.getId());
        continuation.setSourcePermanentSnapshot(sacrificedSnapshot);
        revealHandler.resolve(gameData, continuation,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardSharesCreatureTypeWithSourcePredicate(),
                        LibrarySearchDestination.BATTLEFIELD));
    }
}
