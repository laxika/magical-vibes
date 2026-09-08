package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the Reservoir Kraken beginning-of-combat choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.isTapped()) {
            return;
        }

        List<UUID> opponents = new ArrayList<>(
                AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(
                        gameData, entry.getControllerId()));
        opponents.removeIf(id -> untappedCreatureIds(gameData, id).isEmpty());
        if (opponents.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(
                e.tokenTemplate(), List.copyOf(opponents), entry.getControllerId(),
                entry.getSourcePermanentId(), false));
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        String prompt = "Tap an untapped creature? If you do, " + sourceCard.getName()
                + " becomes tapped and you create a token.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                prompt,
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} tap-a-creature choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public List<UUID> untappedCreatureIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    public void accept(GameData gameData, Card sourceCard,
                       AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect effect,
                       UUID chooserId, UUID creatureId) {
        if (!tapCreature(gameData, sourceCard, chooserId, creatureId)) {
            advance(gameData, sourceCard, effect, chooserId, effect.anyAccepted());
            return;
        }

        if (!effect.anyAccepted()) {
            tapAndCreateToken(gameData, sourceCard, effect);
        }
        advance(gameData, sourceCard, effect, chooserId, true);
    }

    private boolean tapCreature(GameData gameData, Card sourceCard, UUID playerId, UUID creatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        Permanent creature = battlefield.stream()
                .filter(permanent -> permanent.getId().equals(creatureId))
                .findFirst()
                .orElse(null);
        if (creature == null || creature.isTapped()
                || !gameQueryService.isCreature(gameData, creature)) {
            return false;
        }

        tapUntapSupport.tapPermanent(gameData, creature);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " taps ", creature.getCard(),
                " for " + sourceCard.getName() + "'s ability."));
        return true;
    }

    private void tapAndCreateToken(GameData gameData, Card sourceCard,
                                   AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        if (source != null && !source.isTapped()) {
            tapUntapSupport.tapPermanent(gameData, source);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is tapped."));
        }

        StackEntry tokenEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                effect.abilityControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect.tokenTemplate())));
        createTokenEffectHandler.resolve(gameData, tokenEntry, effect.tokenTemplate());
    }

    public void advance(GameData gameData, Card sourceCard,
                        AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect effect,
                        UUID chooserId, boolean anyAccepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> untappedCreatureIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(
                    effect.tokenTemplate(), List.copyOf(remaining), effect.abilityControllerId(),
                    effect.sourcePermanentId(), anyAccepted));
        }
    }
}
