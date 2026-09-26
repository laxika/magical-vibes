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
            UUID attackedPlayerId = entry.getAttackedTargetId();
            if (attacker == null || !gameQueryService.isCreature(gameData, attacker)
                    || attackedPlayerId == null || !gameData.playerIds.contains(attackedPlayerId)) {
                return;
            }

            for (UUID opponentId : gameData.orderedPlayerIds) {
                if (opponentId.equals(controllerId) || opponentId.equals(attackedPlayerId)) {
                    continue;
                }
                if (!effect.mayCreate()) {
                    createTokenCopy(gameData, entry, effect, attacker, liveAttacker, controllerId, opponentId);
                    continue;
                }
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        entry.getCard(), controllerId,
                        List.of(new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
                                opponentId, true, effect.removeLegendary(), effect.exileAtEndStep(),
                                effect.exileAtEndOfCombat())),
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

        createTokenCopy(gameData, entry, effect, attacker, liveAttacker, controllerId, effect.opponentId());
    }

    private void createTokenCopy(GameData gameData, StackEntry entry,
                                 CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect effect,
                                 Permanent attacker, Permanent liveAttacker, UUID controllerId, UUID opponentId) {
        var tokenCopyEffect = effect.exileAtEndOfCombat()
                ? CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingExiledAtEndOfCombat(
                        effect.removeLegendary())
                : CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingCopy(
                        effect.removeLegendary(), effect.exileAtEndStep());
        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(attacker.getCard()),
                liveAttacker == attacker ? liveAttacker : null,
                controllerId,
                tokenCopyEffect,
                List.of(opponentId));
    }

    private Permanent copyableAttacker(GameData gameData, Permanent liveAttacker, StackEntry entry) {
        if (liveAttacker != null && gameQueryService.isCreature(gameData, liveAttacker)) {
            return liveAttacker;
        }
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        return snapshot != null && snapshot.getCard().hasType(CardType.CREATURE) ? snapshot : null;
    }
}
