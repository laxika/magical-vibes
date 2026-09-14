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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Kharasha Foothills' per-opponent attack trigger. */
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
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        entry.getCard(), controllerId,
                        List.of(new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(opponentId)),
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

        int tokenCount = gameQueryService.getTokenCreationAmount(
                gameData,
                controllerId,
                1,
                attacker.getCard().getSubtypes() == null ? List.of() : attacker.getCard().getSubtypes(),
                true);
        if (tokenCount <= 0) {
            return;
        }

        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(attacker.getCard()),
                liveAttacker == attacker ? liveAttacker : null,
                controllerId,
                new CreateTokenCopyOfTargetPermanentEffect(false, true, false, true),
                Collections.nCopies(tokenCount, effect.opponentId()));
    }

    private Permanent copyableAttacker(GameData gameData, Permanent liveAttacker, StackEntry entry) {
        if (liveAttacker != null && gameQueryService.isCreature(gameData, liveAttacker)) {
            return liveAttacker;
        }
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        return snapshot != null && snapshot.getCard().hasType(CardType.CREATURE) ? snapshot : null;
    }
}
