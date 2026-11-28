package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.model.EachPlayerPayManaState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Asks one amount prompt per payer who could pay anything, resolving again at its own index for each answer, and
 * records the payments once the last payer has answered. A mana prompt lets its player activate mana abilities while
 * it is open; an amount the pool can't cover yet is asked again. Each answer's payment is the first change of the run
 * that answers it, so a life payment's Lich's Duel Mastery question is asked first and the payment made again with
 * the same amount.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private static final ManaCost X_COST = new ManaCost("{X}");

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final PotentialManaService potentialManaService;
    private final SubjectResolutionService subjectResolutionService;
    private final ResolutionCostPaymentService costPayment;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaEffect.class;
    }

    /** Only paying life can kill: a mana or energy payment never asks a death replacement. */
    @Override
    public boolean asksDeathReplacementsFirst(CardEffect effect) {
        return ((EachPlayerPaysAnyManaEffect) effect).resource() == Resource.LIFE;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPaysAnyManaEffect pay = (EachPlayerPaysAnyManaEffect) effect;
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        if (!state.active) {
            state.reset();
            state.active = true;
            seedOrder(gameData, state, pay.startingWithYou() ? entry.getControllerId() : gameData.activePlayerId,
                    new HashSet<>(subjectResolutionService.players(gameData, entry, pay, pay.payers())));
            promptOrRecord(gameData, entry, pay);
            return;
        }
        if (gameData.chosenXValue == null) {
            return;
        }
        int amount = gameData.chosenXValue;
        UUID playerId = state.currentPlayerId;
        String playerName = gameData.playerIdToName.get(playerId);
        String cardName = entry.getCard().getName();
        if (amount < 0 || amount > payableNow(gameData, playerId, pay.resource())) {
            gameData.chosenXValue = null;
            if (pay.resource().isMana()) {
                gameLogService.append(gameData, GameLog.text(playerName + " can't pay {" + amount + "} for "
                        + cardName + " (tap mana sources, then choose again)."));
            }
            prompt(gameData, playerId, cardName, pay.resource());
            return;
        }
        if (amount > 0) {
            payNow(gameData, playerId, amount, pay.resource(), cardName);
            state.manaPaid.merge(playerId, amount, Integer::sum);
        }
        gameData.chosenXValue = null;
        gameLogService.append(gameData, GameLog.text(playerName + paidText(amount, pay.resource()) + cardName + "."));
        state.index++;
        promptOrRecord(gameData, entry, pay);
    }

    private static void seedOrder(GameData gameData, EachPlayerPayManaState state, UUID firstPlayerId,
                                  Set<UUID> payers) {
        List<UUID> ordered = gameData.orderedPlayerIds;
        int size = ordered.size();
        int start = Math.max(0, ordered.indexOf(firstPlayerId));
        for (int i = 0; i < size; i++) {
            UUID playerId = ordered.get((start + i) % size);
            if (payers.contains(playerId)) {
                state.order.add(playerId);
                state.manaPaid.put(playerId, 0);
            }
        }
    }

    /** A payer who can't pay anything isn't asked. */
    private void promptOrRecord(GameData gameData, StackEntry entry, EachPlayerPaysAnyManaEffect pay) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotential(gameData, playerId, pay.resource()) > 0) {
                prompt(gameData, playerId, entry.getCard().getName(), pay.resource());
                return;
            }
            state.index++;
        }
        entry.recordStep(pay.resource().stepKind(), StepOutcome.ofAmounts(state.manaPaid));
        state.reset();
    }

    private void prompt(GameData gameData, UUID playerId, String cardName, Resource resource) {
        gameData.eachPlayerPayMana.currentPlayerId = playerId;
        int max = maxPotential(gameData, playerId, resource);
        String prompt = resource == Resource.X_MANA ? "Choose X to pay {X} for " + cardName + "."
                : "Pay any amount of " + resourceName(resource) + " for " + cardName + ".";
        interactionHandlerRegistry.begin(gameData, resource == Resource.LIFE
                ? PendingInteraction.XValueChoice.lifePayment(playerId, max, prompt, cardName)
                : new PendingInteraction.XValueChoice(playerId, max, prompt, cardName, resource.isMana()));
    }

    /**
     * The most the player could pay: for mana, the pool plus what untapped sources could add, since mana abilities
     * may be activated during the payment (CR 605.3a).
     */
    private int maxPotential(GameData gameData, UUID playerId, Resource resource) {
        if (!resource.isMana()) {
            return payableNow(gameData, playerId, resource);
        }
        ManaPool potential = potentialManaService.buildVirtualManaPoolForSpecialAction(gameData, playerId);
        if (resource == Resource.X_MANA) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            potential.addXCostOnlyColorless((pool == null ? 0 : pool.getXCostOnlyColorless())
                    + potentialManaService.potentialXCostOnlyMana(gameData, playerId));
        }
        return payableFrom(potential, resource);
    }

    /** What the player can pay in full right now (CR 118.3): the pool, their energy, their life (CR 119.4). */
    private int payableNow(GameData gameData, UUID playerId, Resource resource) {
        return switch (resource) {
            case MANA, X_MANA -> {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                yield pool == null ? 0 : payableFrom(pool, resource);
            }
            case ENERGY -> gameData.playerEnergyCounters.getOrDefault(playerId, 0);
            case LIFE -> gameQueryService.canPlayerLoseLife(gameData, playerId)
                    ? Math.max(0, gameData.getLife(playerId)) : 0;
        };
    }

    /**
     * The largest amount of mana {@code pool} pays as a resolution-time cost: not a spell, so Powerstone mana pays it;
     * {@code X_MANA} is a cost with {X}, so X-only mana pays it too.
     */
    private static int payableFrom(ManaPool pool, Resource resource) {
        if (resource == Resource.X_MANA) {
            return X_COST.calculateMaxXForResolutionCost(pool);
        }
        int low = 0;
        int high = Math.max(0, pool.getTotalAllMana());
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (new ManaCost("{" + candidate + "}").canPayResolutionCost(pool)) {
                low = candidate;
            } else {
                high = candidate - 1;
            }
        }
        return low;
    }

    /** Pays {@code amount}; no payment of this effect is a tax (Tax Taker), so none names a taxing player. */
    private void payNow(GameData gameData, UUID playerId, int amount, Resource resource, String cardName) {
        switch (resource) {
            case MANA -> costPayment.payManaFromPool(gameData, playerId, new ManaCost("{" + amount + "}"), playerId);
            case X_MANA -> costPayment.payManaFromPool(gameData, playerId, X_COST, amount, playerId);
            case ENERGY -> gameData.removePlayerEnergyCounters(playerId, amount);
            case LIFE -> lifeSupport.applyLifePayment(gameData, playerId, amount, cardName);
        }
    }

    private static String paidText(int amount, Resource resource) {
        if (amount == 0) {
            return " pays no " + resourceName(resource) + " for ";
        }
        return switch (resource) {
            case MANA, X_MANA -> " pays {" + amount + "} for ";
            case ENERGY -> " pays " + amount + " energy for ";
            case LIFE -> " pays " + amount + " life for ";
        };
    }

    private static String resourceName(Resource resource) {
        return switch (resource) {
            case MANA, X_MANA -> "mana";
            case ENERGY -> "energy";
            case LIFE -> "life";
        };
    }
}
