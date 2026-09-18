package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Descendants' Fury's choice among the creatures from the triggering damage event. */
@Component
@RequiredArgsConstructor
public class SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect) effect;
        List<UUID> validIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (typedEffect.combatDamageDealerIds().contains(permanent.getId())
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            return;
        }

        if (validIds.size() == 1) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            if (chosen != null) {
                resolveAfterChoice(gameData, entry, chosen);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreature(
                        entry.getControllerId(), entry.getCard(), typedEffect));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                entry.getCard().getName() + " — Choose one of those creatures to sacrifice.");
    }

    public void resolveAfterChoice(GameData gameData, PermanentChoiceContext context, Permanent chosen) {
        var choice = (PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreature) context;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                choice.sourceCard(),
                choice.controllerId(),
                choice.sourceCard().getName() + "'s ability",
                List.of(),
                null,
                chosen.getId());
        resolveAfterChoice(gameData, entry, chosen);
    }

    private void resolveAfterChoice(GameData gameData, StackEntry entry, Permanent chosen) {
        Permanent sacrificedSnapshot = new Permanent(chosen);
        destructionSupport.sacrificeAndLog(gameData, chosen, entry.getControllerId());

        entry.setSourcePermanentSnapshot(sacrificedSnapshot);
        revealHandler.resolve(gameData, entry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSharesCreatureTypeWithSourcePredicate())),
                        LibrarySearchDestination.BATTLEFIELD));
    }
}
