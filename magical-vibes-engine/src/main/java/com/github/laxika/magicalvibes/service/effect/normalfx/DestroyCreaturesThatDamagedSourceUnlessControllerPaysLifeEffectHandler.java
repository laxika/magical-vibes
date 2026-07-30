package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect}: every creature
 * still on the battlefield that dealt damage to the source card this turn gets an independent
 * pay-or-be-destroyed decision, made by that creature's controller. Damage history is read from
 * {@code GameData.creatureCardsDamagedBySourceThatDiedThisTurn}, which maps a damaging permanent to
 * the cards of the creatures it damaged that have since died, so the lookup is the inverse of that
 * map. The live {@code creatureCardsDamagedThisTurnBySourcePermanent} map can't be used here: a dying
 * creature's card id is purged from it as its death triggers are collected.
 *
 * <p>Decisions are offered one at a time (remaining permanent ids live in
 * {@link GameData#destroyDamagersUnlessPaysRemaining}); a controller who can't pay has their creature
 * destroyed straight away, and destruction always ignores regeneration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect) effect;
        List<UUID> damagers = damagersOf(gameData, entry.getCard().getId());

        gameData.destroyDamagersUnlessPaysRemaining.clear();
        if (damagers.isEmpty()) {
            return;
        }
        gameData.destroyDamagersUnlessPaysRemaining.addAll(damagers.subList(1, damagers.size()));
        offerNext(gameData, e, entry.getCard(), damagers.getFirst());
    }

    /**
     * After a decision on one damaging creature: destroy it when its controller declined or couldn't
     * pay, then offer the next queued creature. Called from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterCreatureDecision(GameData gameData, PendingMayAbility ability,
            DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect, boolean paid) {
        if (!paid) {
            destroyCreature(gameData, ability.sourceCard(), ability.targetCardId());
        }
        if (gameData.destroyDamagersUnlessPaysRemaining.isEmpty()) {
            return;
        }
        offerNext(gameData, effect, ability.sourceCard(),
                gameData.destroyDamagersUnlessPaysRemaining.removeFirst());
    }

    /**
     * Offers the pay-or-destroy prompt for {@code permanentId}, skipping ahead through the queue while
     * a creature has left the battlefield or its controller can't pay (destroying the latter outright).
     */
    private void offerNext(GameData gameData, DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect,
            Card sourceCard, UUID permanentId) {
        UUID current = permanentId;
        while (true) {
            Permanent perm = gameQueryService.findPermanentById(gameData, current);
            UUID controllerId = perm == null ? null : gameQueryService.findPermanentController(gameData, current);
            if (perm != null) {
                boolean canPay = controllerId != null
                        && gameQueryService.canPlayerLifeChange(gameData, controllerId)
                        && gameData.getLife(controllerId) >= effect.lifeCost();
                if (canPay) {
                    String prompt = "Pay " + effect.lifeCost() + " life? If you don't, "
                            + perm.getCard().getName() + " is destroyed and can't be regenerated. ("
                            + sourceCard.getName() + ")";
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            sourceCard, controllerId, List.of(effect), prompt, current));
                    return;
                }
                destroyCreature(gameData, sourceCard, current);
            }
            if (gameData.destroyDamagersUnlessPaysRemaining.isEmpty()) {
                return;
            }
            current = gameData.destroyDamagersUnlessPaysRemaining.removeFirst();
        }
    }

    /** Destroy {@code permanentId} ignoring regeneration, attributing it to {@code sourceCard}. */
    private void destroyCreature(GameData gameData, Card sourceCard, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, target, sourceCard.getName(), true);
    }

    /**
     * Creatures currently on the battlefield that dealt damage to {@code damagedCardId} this turn, in
     * APNAP order so simultaneous decisions are offered in turn order.
     */
    private List<UUID> damagersOf(GameData gameData, UUID damagedCardId) {
        List<UUID> damagers = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                Set<UUID> damaged = gameData.creatureCardsDamagedBySourceThatDiedThisTurn.get(perm.getId());
                if (damaged != null && damaged.contains(damagedCardId)
                        && gameQueryService.isCreature(gameData, perm)) {
                    damagers.add(perm.getId());
                }
            }
        }
        return damagers;
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
