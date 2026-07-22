package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AnyOpponentMayTakeDamageSacrificeSourceEffect} (Vexing Devil). Opponents in turn
 * order may each accept to be dealt the damage; if any accept, the source is sacrificed after all
 * choices (sacrifice still happens if the damage is prevented). Accept/decline lives in
 * {@code mayfx/AnyOpponentMayTakeDamageSacrificeSourceHandler}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMayTakeDamageSacrificeSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMayTakeDamageSacrificeSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyOpponentMayTakeDamageSacrificeSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = apnapOpponents(gameData, controllerId);
        if (opponents.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyOpponentMayTakeDamageSacrificeSourceEffect(
                e.damage(),
                List.copyOf(opponents),
                controllerId,
                entry.getSourcePermanentId(),
                false));
    }

    /**
     * Enqueues a may-ability for the first remaining opponent. Callers must pass an effect whose
     * {@code remainingOpponentIds} is non-empty.
     */
    public void promptNext(GameData gameData, Card sourceCard,
            AnyOpponentMayTakeDamageSacrificeSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        String prompt = "Have " + sourceCard.getName() + " deal " + effect.damage()
                + " damage to you? If you do, it is sacrificed.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                prompt,
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} take-damage choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    /**
     * Deals the effect's damage from the source to {@code opponentId}. Prevention/redirect still
     * count as the opponent having chosen to take the damage for the sacrifice clause.
     */
    public void dealDamage(GameData gameData, PendingMayAbility ability,
            AnyOpponentMayTakeDamageSacrificeSourceEffect effect, UUID opponentId) {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                opponentId,
                effect.sourcePermanentId());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(opponentId) + " chooses to be dealt damage by ",
                ability.sourceCard(), "."));
    }

    /** Sacrifices the source permanent if it is still on the battlefield. */
    public void sacrificeSource(GameData gameData, PendingMayAbility ability,
            AnyOpponentMayTakeDamageSacrificeSourceEffect effect) {
        UUID sourceId = effect.sourcePermanentId();
        if (sourceId == null) {
            return;
        }
        Permanent self = gameQueryService.findPermanentById(gameData, sourceId);
        if (self == null) {
            return;
        }
        SacrificeSelfEffect sac = new SacrificeSelfEffect();
        StackEntry sacEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(sac)),
                (UUID) null,
                sourceId);
        sacrificeSelfEffectHandler.resolve(gameData, sacEntry, sac);
    }

    /** Opponents of {@code controllerId} in turn order starting with the active player (APNAP). */
    public static List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        List<UUID> opponents = new ArrayList<>();
        for (UUID id : rotated) {
            if (!id.equals(controllerId)) {
                opponents.add(id);
            }
        }
        return opponents;
    }
}
