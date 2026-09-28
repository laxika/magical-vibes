package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPartyThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Stick Together's party choices and sacrifice sweep. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesPartyThenSacrificesRestEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardSubtype> PARTY_ROLES = List.of(
            CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD);

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesPartyThenSacrificesRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> playerIds = apnapPlayers(gameData).stream()
                .filter(playerId -> gameQueryService.canEffectCauseSacrifice(
                        gameData, playerId, entry.getControllerId()))
                .toList();
        step(gameData, playerIds, 0, 0, List.of(), entry.getCard().getName());
    }

    /** Continues the current player's role choices after an optional selection. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesPartyThenSacrificesRestChoice context) {
        List<UUID> keptIds = new ArrayList<>(context.keptIds());
        if (!chosenIds.isEmpty()) {
            UUID chosenId = chosenIds.getFirst();
            Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
            UUID playerId = context.playerIds().get(context.playerIndex());
            CardSubtype role = PARTY_ROLES.get(context.roleIndex());
            if (chosen != null
                    && playerId.equals(gameQueryService.findPermanentController(gameData, chosenId))
                    && gameQueryService.isCreature(gameData, chosen)
                    && isPartyRole(gameData, chosen, role)
                    && !keptIds.contains(chosenId)) {
                keptIds.add(chosenId);
            }
        }

        step(gameData, context.playerIds(), context.playerIndex(), context.roleIndex() + 1,
                keptIds, context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex, int roleIndex,
                      List<UUID> keptIds, String sourceName) {
        List<UUID> kept = new ArrayList<>(keptIds);
        int currentPlayerIndex = playerIndex;
        int currentRoleIndex = roleIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            while (currentRoleIndex < PARTY_ROLES.size()) {
                CardSubtype role = PARTY_ROLES.get(currentRoleIndex);
                List<UUID> candidates = candidates(gameData, playerId, role, kept);
                currentRoleIndex++;
                if (candidates.isEmpty()) {
                    continue;
                }

                playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, 1,
                        new MultiPermanentChoiceContext.EachPlayerChoosesPartyThenSacrificesRestChoice(
                                playerIds, currentPlayerIndex, currentRoleIndex - 1, kept, sourceName),
                        sourceName + " — choose up to one " + role.name().toLowerCase()
                                + " for your party.");
                return;
            }
            currentPlayerIndex++;
            currentRoleIndex = 0;
        }

        sacrificeRest(gameData, playerIds, kept);
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardSubtype role,
                                  List<UUID> keptIds) {
        Set<UUID> kept = new HashSet<>(keptIds);
        List<UUID> candidates = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return candidates;
        }
        for (Permanent permanent : battlefield) {
            if (!kept.contains(permanent.getId())
                    && gameQueryService.isCreature(gameData, permanent)
                    && isPartyRole(gameData, permanent, role)) {
                candidates.add(permanent.getId());
            }
        }
        return candidates;
    }

    private boolean isPartyRole(GameData gameData, Permanent permanent, CardSubtype role) {
        return gameQueryService.effectiveCreatureSubtypes(gameData, permanent).contains(role)
                || gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING);
    }

    private void sacrificeRest(GameData gameData, List<UUID> playerIds, List<UUID> keptIds) {
        Set<UUID> kept = new HashSet<>(keptIds);
        List<UUID> toSacrifice = new ArrayList<>();
        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && !kept.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
