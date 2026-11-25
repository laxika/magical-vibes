package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect}: every creature
 * still on the battlefield that dealt damage to the source this turn gets an independent
 * pay-or-be-destroyed decision, made by that creature's controller. Damage history is read from the
 * source permanent while it is still on the battlefield, otherwise from the stack entry's source
 * snapshot, so the ability keeps working after the source card has left the graveyard (CR 608.2h).
 *
 * <p>Decisions are offered one at a time in APNAP order (remaining permanent ids live in
 * {@link GameData#destroyDamagersUnlessPaysRemaining}); a controller who can't pay gets no prompt. The
 * unpaid creatures are collected in {@link GameData#destroyDamagersUnlessPaysUnpaid} and destroyed together,
 * ignoring regeneration, once the last decision is made (CR 101.4). That last step changes nothing before the
 * batch's death replacements are asked (CR 616.1): the last answer's life payment runs only once they are.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect) effect;
        gameData.destroyDamagersUnlessPaysRemaining.clear();
        gameData.destroyDamagersUnlessPaysUnpaid.clear();
        gameData.destroyDamagersUnlessPaysRemaining.addAll(damagersOf(gameData, entry));
        advance(gameData, e, entry.getCard(), null, null, () -> { });
    }

    /**
     * After a decision on one damaging creature: offers the next queued creature, or, after the last one,
     * destroys every unpaid creature together. {@code payment} is the answer's life payment ({@code paid}), run
     * before the next prompt or, after the last decision, once the batch's death replacements are asked. Called
     * from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterCreatureDecision(GameData gameData, PendingMayAbility ability,
            DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect, boolean paid, Runnable payment) {
        advance(gameData, effect, ability.sourceCard(), paid ? ability.controllerId() : null,
                paid ? null : ability.targetCardId(), payment);
    }

    /**
     * Finds the next queued creature whose controller can pay, counting the life {@code payerId} is about to pay,
     * before changing anything: the creatures skipped on the way (gone, or their controller can't pay) and the
     * {@code declinedId} just decided are recorded unpaid only once it is known whether a prompt follows.
     */
    private void advance(GameData gameData, DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect,
            Card sourceCard, UUID payerId, UUID declinedId, Runnable payment) {
        List<UUID> queue = gameData.destroyDamagersUnlessPaysRemaining;
        List<UUID> unpaid = new ArrayList<>();
        if (declinedId != null) {
            unpaid.add(declinedId);
        }
        int scanned = 0;
        UUID next = null;
        UUID nextControllerId = null;
        for (UUID permanentId : queue) {
            scanned++;
            Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
            if (perm == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
            int committed = controllerId != null && controllerId.equals(payerId) ? effect.lifeCost() : 0;
            if (controllerId != null && gameQueryService.canPlayerLoseLife(gameData, controllerId)
                    && gameData.getLife(controllerId) - committed >= effect.lifeCost()) {
                next = permanentId;
                nextControllerId = controllerId;
                break;
            }
            unpaid.add(permanentId);
        }

        if (next != null) {
            gameData.destroyDamagersUnlessPaysUnpaid.addAll(unpaid);
            queue.subList(0, scanned).clear();
            payment.run();
            Permanent perm = gameQueryService.findPermanentById(gameData, next);
            String prompt = "Pay " + effect.lifeCost() + " life? If you don't, "
                    + FaceDownVisibility.publicName(gameData, perm) + " is destroyed and can't be regenerated. ("
                    + sourceCard.getName() + ")";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard, nextControllerId, List.of(effect), prompt, next));
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        for (UUID permanentId : concat(gameData.destroyDamagersUnlessPaysUnpaid, unpaid)) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
            if (perm != null) {
                toDestroy.add(perm);
            }
        }
        Runnable finish = () -> {
            payment.run();
            queue.clear();
            gameData.destroyDamagersUnlessPaysUnpaid.clear();
        };
        if (toDestroy.isEmpty()) {
            finish.run();
            return;
        }
        destructionSupport.destroyBatch(gameData, toDestroy, sourceCard.getName(), true, finish);
    }

    private static List<UUID> concat(List<UUID> first, List<UUID> second) {
        List<UUID> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    /**
     * Creatures currently on the battlefield that dealt damage to the entry's source this turn, in
     * APNAP order so simultaneous decisions are offered in turn order.
     */
    private List<UUID> damagersOf(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        List<UUID> damagers = new ArrayList<>();
        if (source == null) {
            return damagers;
        }
        Set<UUID> damagerIds = source.getPermanentIdsThatDealtDamageThisTurn();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                if (damagerIds.contains(perm.getId()) && gameQueryService.isCreature(gameData, perm)) {
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
