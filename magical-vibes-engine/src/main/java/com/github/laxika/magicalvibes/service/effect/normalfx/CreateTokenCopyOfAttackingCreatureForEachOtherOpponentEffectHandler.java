package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves per-opponent attacking-copy triggers. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect rawEffect) {
        var effect = (CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect) rawEffect;
        UUID attackerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        Permanent liveAttacker = attackerId == null
                ? null : gameQueryService.findPermanentById(gameData, attackerId);
        Permanent attacker = copyableAttacker(gameData, liveAttacker, entry);

        if (effect.opponentId() == null) {
            UUID attackedPlayerId = liveAttacker != null && liveAttacker.getAttackTarget() != null
                    ? liveAttacker.getAttackTarget() : entry.getAttackedTargetId();
            if (attackedPlayerId != null && !gameData.playerIds.contains(attackedPlayerId)) {
                Permanent attackedPermanent = gameQueryService.findPermanentById(gameData, attackedPlayerId);
                attackedPlayerId = attackedPermanent != null && attackedPermanent.getCard().hasType(CardType.BATTLE)
                        ? attackedPermanent.getProtectorPlayerId()
                        : attackedPermanent != null
                        ? gameQueryService.findPermanentController(gameData, attackedPlayerId)
                        : entry.getDefendingPlayerId();
            }
            if (attacker == null || !gameQueryService.isCreature(gameData, attacker)
                    || attackedPlayerId == null || !gameData.playerIds.contains(attackedPlayerId)) {
                return;
            }

            UUID defendingPlayer = attackedPlayerId;
            List<UUID> opponents = gameData.orderedPlayerIds.stream()
                    .filter(id -> !id.equals(controllerId) && !id.equals(defendingPlayer)).toList();
            if (opponents.isEmpty()) return;
            if (!effect.mayCreate()) {
                createTokenCopies(gameData, entry, effect, attacker, liveAttacker, controllerId, opponents);
                return;
            }
            UUID batchId = UUID.randomUUID();
            gameData.pendingAttackingCopyOpponents.put(batchId, new ArrayList<>());
            gameData.pendingAttackingCopyChoices.put(batchId, opponents.size());
            for (UUID opponentId : opponents) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        entry.getCard(), controllerId,
                        List.of(new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
                                opponentId, true, effect.removeLegendary(), effect.exileAtEndStep(),
                                effect.exileAtEndOfCombat(), batchId)),
                        "Create a tapped and attacking token copy of " + attacker.getCard().getName()
                                + " attacking " + gameData.playerIdToName.get(opponentId) + "?",
                        attackerId,
                        null,
                        attackerId,
                        null,
                        0,
                        0,
                        attackedPlayerId,
                        entry.getActivePlayerId(),
                        null,
                        new Permanent(attacker),
                        entry.getTriggeringPermanentControllerId(),
                        entry.getTriggeringCardId(),
                        entry.getEventValue()));
            }
            return;
        }

        if (attacker == null || !gameQueryService.isCreature(gameData, attacker)
                || !gameData.playerIds.contains(effect.opponentId())) {
            return;
        }

        completeChoice(gameData, entry, effect, true);
    }

    /** Collects per-opponent decisions before placing every accepted copy in one battlefield event. */
    public void completeChoice(GameData gameData, StackEntry entry,
                               CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect effect,
                               boolean accepted) {
        List<UUID> opponents;
        if (effect.choiceBatchId() == null) {
            if (!accepted) return;
            opponents = List.of(effect.opponentId());
        } else {
            opponents = gameData.pendingAttackingCopyOpponents.get(effect.choiceBatchId());
            if (opponents == null) return;
            if (accepted) opponents.add(effect.opponentId());
            int remaining = gameData.pendingAttackingCopyChoices.computeIfPresent(
                    effect.choiceBatchId(), (ignored, count) -> count - 1);
            if (remaining > 0) return;
            gameData.pendingAttackingCopyChoices.remove(effect.choiceBatchId());
            gameData.pendingAttackingCopyOpponents.remove(effect.choiceBatchId());
        }
        if (opponents.isEmpty()) return;
        Permanent liveAttacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent attacker = copyableAttacker(gameData, liveAttacker, entry);
        if (attacker == null) return;
        createTokenCopies(gameData, entry, effect, attacker, liveAttacker, entry.getControllerId(), opponents);
    }

    private void createTokenCopies(GameData gameData, StackEntry entry,
                                 CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect effect,
                                 Permanent attacker, Permanent liveAttacker, UUID controllerId, List<UUID> opponents) {
        var tokenCopyEffect = effect.exileAtEndOfCombat()
                ? CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingExiledAtEndOfCombat(
                        effect.removeLegendary())
                : CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingCopy(
                        effect.removeLegendary(), effect.exileAtEndStep());
        if (!effect.exileAtEndOfCombat()) {
            tokenCopySupport.createTokenCopies(gameData, entry,
                    opponents.stream().map(ignored -> attacker.getCard()).toList(),
                    liveAttacker == attacker ? liveAttacker : null, controllerId, tokenCopyEffect, opponents);
            return;
        }
        tokenCopySupport.createTokenCopiesChoosingOpponentAttackTargets(
                gameData,
                entry,
                opponents.stream().map(ignored -> attacker.getCard()).toList(),
                liveAttacker == attacker ? liveAttacker : null,
                controllerId,
                tokenCopyEffect,
                opponents);
    }

    private Permanent copyableAttacker(GameData gameData, Permanent liveAttacker, StackEntry entry) {
        if (liveAttacker != null && gameQueryService.isCreature(gameData, liveAttacker)) {
            return liveAttacker;
        }
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        return snapshot != null && snapshot.getCard().hasType(CardType.CREATURE) ? snapshot : null;
    }
}
