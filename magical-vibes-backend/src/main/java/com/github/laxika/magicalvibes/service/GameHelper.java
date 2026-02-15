package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingOrBlockingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AttackingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TappedTargetFilter;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameHelper {

    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final CardViewFactory cardViewFactory;
    private final PermanentViewFactory permanentViewFactory;
    private final StackEntryViewFactory stackEntryViewFactory;

    static final List<String> TEXT_CHANGE_COLOR_WORDS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    static final List<String> TEXT_CHANGE_LAND_TYPES = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");

    // ===== Package-private accessors for sibling services =====

    SessionManager getSessionManager() {
        return sessionManager;
    }

    CardViewFactory getCardViewFactory() {
        return cardViewFactory;
    }

    StackEntryViewFactory getStackEntryViewFactory() {
        return stackEntryViewFactory;
    }

    // ===== Query methods =====

    Permanent findPermanentById(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    Card findCardInGraveyardById(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(cardId)) {
                    return c;
                }
            }
        }
        return null;
    }

    UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    public boolean isCreature(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getType() != CardType.ARTIFACT) return false;
        return hasAnimateArtifactEffect(gameData);
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return permanent.hasKeyword(keyword) || computeStaticBonus(gameData, permanent).keywords().contains(keyword);
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return permanent.getEffectivePower() + computeStaticBonus(gameData, permanent).power();
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return permanent.getEffectiveToughness() + computeStaticBonus(gameData, permanent).toughness();
    }

    record StaticBonus(int power, int toughness, Set<Keyword> keywords, boolean animatedCreature) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), false);
    }

    StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        boolean isNaturalCreature = target.getCard().getType() == CardType.CREATURE;
        boolean isArtifact = target.getCard().getType() == CardType.ARTIFACT;
        boolean animatedCreature = false;
        int power = 0;
        int toughness = 0;
        Set<Keyword> keywords = new HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                if (source == target) continue;
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AnimateNoncreatureArtifactsEffect && isArtifact) {
                        animatedCreature = true;
                    }
                    if (effect instanceof BoostCreaturesBySubtypeEffect boost
                            && (target.hasKeyword(Keyword.CHANGELING)
                                || target.getCard().getSubtypes().stream().anyMatch(boost.affectedSubtypes()::contains))) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                        keywords.addAll(boost.grantedKeywords());
                    }
                    if (effect instanceof BoostEnchantedCreatureEffect boost
                            && source.getAttachedTo() != null
                            && source.getAttachedTo().equals(target.getId())) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                    if (effect instanceof GrantKeywordToEnchantedCreatureEffect grant
                            && source.getAttachedTo() != null
                            && source.getAttachedTo().equals(target.getId())) {
                        keywords.add(grant.keyword());
                    }
                    if (effect instanceof BoostOwnCreaturesEffect boost
                            && bf.contains(target)) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                    if (effect instanceof BoostOtherCreaturesByColorEffect boost
                            && target.getCard().getColor() == boost.color()) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                    if (effect instanceof BoostNonColorCreaturesEffect boost
                            && target.getCard().getColor() != boost.excludedColor()) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                }
            }
        }
        if (!isNaturalCreature && !animatedCreature) return StaticBonus.NONE;

        if (animatedCreature) {
            int manaValue = target.getCard().getManaValue();
            power += manaValue;
            toughness += manaValue;
        }

        return new StaticBonus(power, toughness, keywords, animatedCreature);
    }

    boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) return false;
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection && protection.colors().contains(sourceColor)) {
                return true;
            }
        }
        if (target.getChosenColor() != null && target.getChosenColor() == sourceColor) {
            return true;
        }
        return false;
    }

    boolean playerHasShroud(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent source : battlefield) {
            if (source.getCard().getKeywords().contains(Keyword.SHROUD)) {
                return true;
            }
        }
        return false;
    }

    boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }

    boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effectClass.isInstance(effect)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean isEnchanted(GameData gameData, Permanent creature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().isAura()) {
                    return true;
                }
            }
        }
        return false;
    }

    Permanent findEnchantedCreatureByAuraEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (p.getAttachedTo() != null) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effectClass.isInstance(effect)) {
                        return findPermanentById(gameData, p.getAttachedTo());
                    }
                }
            }
        }
        return null;
    }

    boolean matchesFilters(GameData gameData, Permanent permanent, Set<TargetFilter> filters) {
        for (TargetFilter filter : filters) {
            if (filter instanceof WithoutKeywordTargetFilter f) {
                if (hasKeyword(gameData, permanent, f.keyword())) {
                    return false;
                }
            }
        }
        return true;
    }

    void validateTargetFilter(TargetFilter filter, Permanent target) {
        if (filter instanceof MaxPowerTargetFilter f) {
            if (target.getEffectivePower() > f.maxPower()) {
                throw new IllegalStateException("Target creature's power must be " + f.maxPower() + " or less");
            }
        } else if (filter instanceof AttackingOrBlockingTargetFilter) {
            if (!target.isAttacking() && !target.isBlocking()) {
                throw new IllegalStateException("Target must be an attacking or blocking creature");
            }
        } else if (filter instanceof AttackingTargetFilter) {
            if (!target.isAttacking()) {
                throw new IllegalStateException("Target must be an attacking creature");
            }
        } else if (filter instanceof CreatureColorTargetFilter f) {
            if (!f.colors().contains(target.getCard().getColor())) {
                String colorNames = f.colors().stream()
                        .map(c -> c.name().toLowerCase())
                        .reduce((a, b) -> a + " or " + b)
                        .orElse("");
                throw new IllegalStateException("Target must be a " + colorNames + " creature");
            }
        } else if (filter instanceof TappedTargetFilter) {
            if (!target.isTapped()) {
                throw new IllegalStateException("Target must be a tapped creature");
            }
        }
    }

    boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return hasAuraWithEffect(gameData, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)
                || isDamageFromSourcePrevented(gameData, creature.getCard().getColor());
    }

    boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        return sourceColor != null && gameData.preventDamageFromColors.contains(sourceColor);
    }

    // ===== State view methods =====

    void broadcastGameState(GameData gameData) {
        List<String> newLogEntries;
        int logSize = gameData.gameLog.size();
        if (logSize > gameData.lastBroadcastedLogSize) {
            newLogEntries = new ArrayList<>(gameData.gameLog.subList(gameData.lastBroadcastedLogSize, logSize));
        } else {
            newLogEntries = List.of();
        }
        gameData.lastBroadcastedLogSize = logSize;

        List<List<PermanentView>> battlefields = getBattlefields(gameData);
        List<StackEntryView> stack = getStackViews(gameData);
        List<List<CardView>> graveyards = getGraveyardViews(gameData);
        List<Integer> deckSizes = getDeckSizes(gameData);
        List<Integer> handSizes = getHandSizes(gameData);
        List<Integer> lifeTotals = getLifeTotals(gameData);
        UUID priorityPlayerId = gameData.awaitingInput != null ? null : getPriorityPlayerId(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<CardView> hand = gameData.playerHands.getOrDefault(playerId, List.of())
                    .stream().map(cardViewFactory::create).toList();
            List<CardView> opponentHand = getRevealedOpponentHand(gameData, playerId);
            int mulliganCount = gameData.mulliganCounts.getOrDefault(playerId, 0);
            Map<String, Integer> manaPool = getManaPool(gameData, playerId);
            List<TurnStep> autoStopSteps = gameData.playerAutoStopSteps.containsKey(playerId)
                    ? new ArrayList<>(gameData.playerAutoStopSteps.get(playerId))
                    : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
            List<Integer> playableCardIndices = getPlayableCardIndices(gameData, playerId);

            sessionManager.sendToPlayer(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices, newLogEntries
            ));
        }
    }

    List<StackEntryView> getStackViews(GameData gameData) {
        return gameData.stack.stream().map(stackEntryViewFactory::create).toList();
    }

    List<List<PermanentView>> getBattlefields(GameData data) {
        List<List<PermanentView>> battlefields = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) {
                battlefields.add(new ArrayList<>());
            } else {
                List<PermanentView> views = new ArrayList<>();
                for (Permanent p : bf) {
                    StaticBonus bonus = computeStaticBonus(data, p);
                    views.add(permanentViewFactory.create(p, bonus.power(), bonus.toughness(), bonus.keywords(), bonus.animatedCreature()));
                }
                battlefields.add(views);
            }
        }
        return battlefields;
    }

    List<List<CardView>> getGraveyardViews(GameData data) {
        List<List<CardView>> graveyards = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            graveyards.add(gy != null ? gy.stream().map(cardViewFactory::create).toList() : new ArrayList<>());
        }
        return graveyards;
    }

    List<Integer> getHandSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> hand = data.playerHands.get(pid);
            sizes.add(hand != null ? hand.size() : 0);
        }
        return sizes;
    }

    List<CardView> getRevealedOpponentHand(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return List.of();
        boolean reveals = false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RevealOpponentHandsEffect) {
                    reveals = true;
                    break;
                }
            }
            if (reveals) break;
        }
        if (!reveals) return List.of();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(playerId)) {
                return gameData.playerHands.getOrDefault(opponentId, List.of())
                        .stream().map(cardViewFactory::create).toList();
            }
        }
        return List.of();
    }

    List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    Map<String, Integer> getManaPool(GameData data, UUID playerId) {
        if (playerId == null) {
            return new ManaPool().toMap();
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : new ManaPool().toMap();
    }

    List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            totals.add(gameData.playerLifeTotals.getOrDefault(pid, 20));
        }
        return totals;
    }

    List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.awaitingInput != null) {
            return playable;
        }

        UUID priorityHolder = getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        int spellsCast = gameData.spellsCastThisTurn.getOrDefault(playerId, 0);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;

        boolean stackEmpty = gameData.stack.isEmpty();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.BASIC_LAND && isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                playable.add(i);
            }
            if (card.getType() == CardType.CREATURE && card.getManaCost() != null && !spellLimitReached) {
                boolean hasFlash = card.getKeywords().contains(Keyword.FLASH);
                if (hasFlash || (isActivePlayer && isMainPhase && stackEmpty)) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.CREATURE);
                    if (cost.canPay(pool, additionalCost)) {
                        playable.add(i);
                    }
                }
            }
            if (card.getType() == CardType.ENCHANTMENT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ENCHANTMENT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.ARTIFACT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ARTIFACT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.SORCERY && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.SORCERY);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.INSTANT && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.INSTANT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
        }

        return playable;
    }

    int getMaxSpellsPerTurn(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof LimitSpellsPerTurnEffect limit) {
                        return limit.maxSpells();
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    int getOpponentCostIncrease(GameData gameData, UUID playerId, CardType cardType) {
        UUID opponentId = getOpponentId(gameData, playerId);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.get(opponentId);
        if (opponentBattlefield == null) return 0;

        int totalIncrease = 0;
        for (Permanent perm : opponentBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof IncreaseOpponentCastCostEffect increase) {
                    if (increase.affectedTypes().contains(cardType)) {
                        totalIncrease += increase.amount();
                    }
                }
            }
        }
        return totalIncrease;
    }

    int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return 0;

        int totalTax = 0;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToAttackEffect tax) {
                    totalTax += tax.amountPerAttacker();
                }
            }
        }
        return totalTax;
    }

    // ===== Lifecycle methods =====

    boolean removePermanentToGraveyard(GameData gameData, Permanent target) {
        boolean wasCreature = isCreature(gameData, target);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.playerGraveyards.get(graveyardOwnerId).add(target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
                collectDeathTrigger(gameData, target.getCard(), playerId, wasCreature);
                return true;
            }
        }
        return false;
    }

    void removeOrphanedAuras(GameData gameData) {
        boolean anyRemoved = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.getAttachedTo() != null && findPermanentById(gameData, p.getAttachedTo()) == null) {
                    it.remove();
                    gameData.playerGraveyards.get(playerId).add(p.getOriginalCard());
                    String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                    anyRemoved = true;
                }
            }
        }
        if (anyRemoved) {

        }
        returnStolenCreatures(gameData);
    }

    void removeCardFromGraveyardById(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                return;
            }
        }
    }

    void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        if (!wasCreature) return;

        List<CardEffect> deathEffects = dyingCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects.isEmpty()) return;

        for (CardEffect effect : deathEffects) {
            if (effect instanceof MayEffect may) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        dyingCard,
                        controllerId,
                        List.of(may.wrapped()),
                        dyingCard.getName() + " — " + may.prompt()
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dyingCard,
                        controllerId,
                        dyingCard.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
            }
        }
    }

    boolean checkWinCondition(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            if (life <= 0) {
                UUID winnerId = getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                logAndBroadcast(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life);
                return true;
            }
        }
        return false;
    }

    void resetEndOfTurnModifiers(GameData gameData) {
        boolean anyReset = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                        || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()) {
                    p.resetModifiers();
                    p.setDamagePreventionShield(0);
                    p.setRegenerationShield(0);
                    anyReset = true;
                }
            }
        }
        if (anyReset) {

        }

        gameData.playerDamagePreventionShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.drawReplacementTargetToController.clear();
    }

    void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (isCreature(gameData, p) && getEffectiveToughness(gameData, p) <= 0) {
                    it.remove();
                    UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(p.getId(), playerId);
                    gameData.stolenCreatures.remove(p.getId());
                    gameData.playerGraveyards.get(graveyardOwnerId).add(p.getOriginalCard());
                    collectDeathTrigger(gameData, p.getCard(), playerId, true);
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                    anyDied = true;
                }
            }
        }
        if (anyDied) {
            removeOrphanedAuras(gameData);
        }
    }

    void drainManaPools(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
            }
        }
    }

    // ===== Prevention methods =====

    int applyGlobalPreventionShield(GameData gameData, int damage) {
        int shield = gameData.globalDamagePreventionShield;
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.globalDamagePreventionShield = shield - prevented;
        return damage - prevented;
    }

    int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
        if (hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.playerDamagePreventionShields.put(playerId, shield - prevented);
        return damage - prevented;
    }

    boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        if (sourceColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        if (colorMap == null) return false;
        Integer count = colorMap.get(sourceColor);
        if (count == null || count <= 0) return false;
        colorMap.put(sourceColor, count - 1);
        return true;
    }

    // ===== Mana =====

    void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestAmount = 0;
            for (ManaColor color : ManaColor.values()) {
                int available = pool.get(color);
                if (available > highestAmount) {
                    highestAmount = available;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remaining--;
            } else {
                break;
            }
        }
    }

    // ===== Clone / Legend =====

    void applyCloneCopy(Permanent clonePerm, Permanent targetPerm) {
        Card target = targetPerm.getCard();
        Card copy = new Card(target.getName(), target.getType(), target.getManaCost(), target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(target.getPower());
        copy.setToughness(target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setNeedsTarget(target.isNeedsTarget());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        copy.setArtist(target.getArtist());
        copy.setRarity(target.getRarity());
        for (EffectSlot slot : EffectSlot.values()) {
            for (CardEffect effect : target.getEffects(slot)) {
                copy.addEffect(slot, effect);
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }
        clonePerm.setCard(copy);
    }

    boolean checkLegendRule(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        Map<String, List<UUID>> legendaryByName = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)) {
                legendaryByName.computeIfAbsent(perm.getCard().getName(), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2) {
                gameData.permanentChoiceContext = new PermanentChoiceContext.LegendRule(entry.getKey());
                beginPermanentChoice(gameData, controllerId, entry.getValue(),
                        "You control multiple legendary permanents named " + entry.getKey() + ". Choose one to keep.");
                return true;
            }
        }
        return false;
    }

    // ===== ETB pipeline =====

    void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        boolean needsCopyChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
        if (needsCopyChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent clonePerm = bf.get(bf.size() - 1);
            gameData.permanentChoiceContext = new PermanentChoiceContext.CloneCopy(clonePerm.getId());

            List<UUID> creatureIds = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (isCreature(gameData, p) && !p.getId().equals(clonePerm.getId())) {
                        creatureIds.add(p.getId());
                    }
                }
            }

            if (!creatureIds.isEmpty()) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(new CopyCreatureOnEnterEffect()),
                        card.getName() + " — You may have it enter as a copy of any creature on the battlefield."
                ));
                processNextMayAbility(gameData);
                return;
            } else {
                gameData.permanentChoiceContext = null;
                performStateBasedActions(gameData);
                return;
            }
        }

        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            beginColorChoice(gameData, controllerId, justEntered.getId(), targetPermanentId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetPermanentId);
    }

    void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                .toList();
        if (!triggeredEffects.isEmpty()) {
            List<CardEffect> mayEffects = triggeredEffects.stream().filter(e -> e instanceof MayEffect).toList();
            List<CardEffect> mandatoryEffects = triggeredEffects.stream().filter(e -> !(e instanceof MayEffect)).toList();

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(may.wrapped()),
                        card.getName() + " — " + may.prompt()
                ));
            }

            if (!mandatoryEffects.isEmpty()) {
                // Separate graveyard exile effects (need multi-target selection at trigger time)
                List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                        .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
                List<CardEffect> otherEffects = mandatoryEffects.stream()
                        .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect)).toList();

                // Put non-graveyard-exile effects on the stack as before
                if (!otherEffects.isEmpty()) {
                    if (!card.isNeedsTarget() || targetPermanentId != null) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ETB ability",
                                new ArrayList<>(otherEffects),
                                0,
                                targetPermanentId,
                                Map.of()
                        ));
                        String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                        logAndBroadcast(gameData, etbLog);
                        log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                    }
                }

                // Handle graveyard exile effects: targets must be chosen at trigger time
                for (CardEffect effect : graveyardExileEffects) {
                    ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
                    handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
                }
            }
        }

        checkAllyCreatureEntersTriggers(gameData, controllerId, card);
        checkAnyCreatureEntersTriggers(gameData, controllerId, card);
    }

    private void handleGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                   List<CardEffect> allEffects, ExileCardsFromGraveyardEffect exile) {
        // Collect all cards from all graveyards
        List<UUID> allCardIds = new ArrayList<>();
        List<CardView> allCardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                allCardIds.add(graveyardCard.getId());
                allCardViews.add(getCardViewFactory().create(graveyardCard));
            }
        }

        if (allCardIds.isEmpty()) {
            // No graveyard cards: put ability on stack with 0 targets (just gains life on resolution)
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
            logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            // Prompt player to choose targets before putting ability on the stack
            int maxTargets = Math.min(exile.maxTargets(), allCardIds.size());
            gameData.pendingGraveyardTargetCard = card;
            gameData.pendingGraveyardTargetControllerId = controllerId;
            gameData.pendingGraveyardTargetEffects = new ArrayList<>(allEffects);
            beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from graveyards to exile.");
        }
    }

    void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEqualToToughnessEffect) {
                    int toughness = enteringCreature.getToughness();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(toughness))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(controllerId) + " will gain " + toughness + " life.";
                    logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                }
            }
        }
    }

    void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                if (perm.getCard() == enteringCreature) continue;

                for (CardEffect effect : effects) {
                    if (effect instanceof GainLifeEffect gainLife) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                List.of(new GainLifeEffect(gainLife.amount()))
                        ));
                        String triggerLog = perm.getCard().getName() + " triggers — " +
                                gameData.playerIdToName.get(playerId) + " will gain " + gainLife.amount() + " life.";
                        logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                    }
                }
            }
        }
    }

    // ===== Input starters =====

    void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingInput = AwaitingInput.CARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetPermanentId) {
        gameData.awaitingInput = AwaitingInput.TARGETED_CARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.pendingCardChoiceTargetPermanentId = targetPermanentId;
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand (targeted)", gameData.id, playerName);
    }

    void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        gameData.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        gameData.awaitingPermanentChoicePlayerId = playerId;
        gameData.awaitingPermanentChoiceValidIds = new HashSet<>(validIds);
        sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(validIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    void beginGraveyardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        gameData.awaitingGraveyardChoicePlayerId = playerId;
        gameData.awaitingGraveyardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from graveyard", gameData.id, playerName);
    }

    void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        gameData.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        gameData.awaitingMultiPermanentChoicePlayerId = playerId;
        gameData.awaitingMultiPermanentChoiceValidIds = new HashSet<>(validIds);
        gameData.awaitingMultiPermanentChoiceMaxCount = maxCount;
        sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(validIds, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanents", gameData.id, playerName, maxCount);
    }

    void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews, int maxCount, String prompt) {
        gameData.awaitingInput = AwaitingInput.MULTI_GRAVEYARD_CHOICE;
        gameData.awaitingMultiGraveyardChoicePlayerId = playerId;
        gameData.awaitingMultiGraveyardChoiceValidCardIds = new HashSet<>(validCardIds);
        gameData.awaitingMultiGraveyardChoiceMaxCount = maxCount;
        sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} cards from graveyards", gameData.id, playerName, maxCount);
    }

    void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetPermanentId) {
        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.awaitingColorChoicePlayerId = playerId;
        gameData.awaitingColorChoicePermanentId = permanentId;
        gameData.pendingColorChoiceETBTargetId = etbTargetPermanentId;
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    void beginDiscardChoice(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.awaitingInput = AwaitingInput.DISCARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, "Choose a card to discard."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card to discard", gameData.id, playerName);
    }

    void beginRevealedHandChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingInput = AwaitingInput.REVEALED_HAND_CHOICE;
        gameData.awaitingCardChoicePlayerId = choosingPlayerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<CardView> cardViews = targetHand.stream().map(getCardViewFactory()::create).toList();
        sessionManager.sendToPlayer(choosingPlayerId, new ChooseFromRevealedHandMessage(cardViews, validIndices, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card from revealed hand", gameData.id, playerName);
    }

    // ===== Control =====

    void stealCreature(GameData gameData, UUID newControllerId, Permanent creature) {
        UUID originalOwnerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(creature)) {
                originalOwnerId = pid;
                break;
            }
        }
        if (originalOwnerId == null || originalOwnerId.equals(newControllerId)) {
            return;
        }

        gameData.playerBattlefields.get(originalOwnerId).remove(creature);
        gameData.playerBattlefields.get(newControllerId).add(creature);
        creature.setSummoningSick(true);

        if (!gameData.stolenCreatures.containsKey(creature.getId())) {
            gameData.stolenCreatures.put(creature.getId(), originalOwnerId);
        }

        String newControllerName = gameData.playerIdToName.get(newControllerId);
        String logEntry = newControllerName + " gains control of " + creature.getCard().getName() + ".";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains control of {}", gameData.id, newControllerName, creature.getCard().getName());
    }

    void returnStolenCreatures(GameData gameData) {
        if (gameData.stolenCreatures.isEmpty()) return;

        boolean anyReturned = false;
        Iterator<Map.Entry<UUID, UUID>> it = gameData.stolenCreatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            UUID creatureId = entry.getKey();
            UUID ownerId = entry.getValue();

            Permanent creature = findPermanentById(gameData, creatureId);
            if (creature == null) {
                it.remove();
                gameData.enchantmentDependentStolenCreatures.remove(creatureId);
                continue;
            }

            if (hasAuraWithEffect(gameData, creature, ControlEnchantedCreatureEffect.class)) {
                continue;
            }

            if (gameData.enchantmentDependentStolenCreatures.contains(creatureId)
                    && isEnchanted(gameData, creature)) {
                continue;
            }
            gameData.enchantmentDependentStolenCreatures.remove(creatureId);

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf != null && bf.remove(creature)) {
                    gameData.playerBattlefields.get(ownerId).add(creature);
                    creature.setSummoningSick(true);

                    String ownerName = gameData.playerIdToName.get(ownerId);
                    String logEntry = creature.getCard().getName() + " returns to " + ownerName + "'s control.";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns to {}'s control", gameData.id, creature.getCard().getName(), ownerName);
                    anyReturned = true;
                    break;
                }
            }
            it.remove();
        }
        if (anyReturned) {

        }
    }

    int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName) {
        if (damage <= 0) return damage;
        Permanent target = findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        if (target == null) return damage;

        int effectiveDamage = applyCreaturePreventionShield(gameData, target, damage);
        String logEntry = target.getCard().getName() + " absorbs " + effectiveDamage + " redirected " + sourceName + " damage.";
        logAndBroadcast(gameData, logEntry);

        if (effectiveDamage >= getEffectiveToughness(gameData, target)) {
            removePermanentToGraveyard(gameData, target);
            String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
            logAndBroadcast(gameData, deathLog);
            removeOrphanedAuras(gameData);
        }

        return 0;
    }

    // ===== Triggers =====

    void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        gameData.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        gameData.awaitingMayAbilityPlayerId = next.controllerId();
        sessionManager.sendToPlayer(next.controllerId(), new MayAbilityMessage(next.description()));

        String playerName = gameData.playerIdToName.get(next.controllerId());
        log.info("Game {} - Awaiting {} to decide on may ability: {}", gameData.id, playerName, next.description());
    }

    void checkSpellCastTriggers(GameData gameData, Card spellCard) {
        if (spellCard.getColor() == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)) {
                    CardEffect inner = effect instanceof MayEffect m ? m.wrapped() : effect;

                    if (inner instanceof GainLifeOnColorSpellCastEffect trigger
                            && spellCard.getColor() == trigger.triggerColor()) {
                        List<CardEffect> resolvedEffects = List.of(new GainLifeEffect(trigger.amount()));

                        if (effect instanceof MayEffect may) {
                            gameData.pendingMayAbilities.add(new PendingMayAbility(
                                    perm.getCard(),
                                    playerId,
                                    resolvedEffects,
                                    perm.getCard().getName() + " — " + may.prompt()
                            ));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s ability",
                                    new ArrayList<>(resolvedEffects)
                            ));
                        }
                    }
                }
            }
        }

        processNextMayAbility(gameData);
    }

    // ===== Draw =====

    void resolveDrawCard(GameData gameData, UUID playerId) {
        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is replaced by Plagiarize — " + controllerName + " draws a card instead.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Plagiarize replaces {}'s draw, {} draws instead",
                    gameData.id, playerName, controllerName);
            performDrawCard(gameData, replacementController);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    void performDrawCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));
    }

    // ===== Logging =====

    void logAndBroadcast(GameData gameData, String logEntry) {
        gameData.gameLog.add(logEntry);
    }

    // ===== Regeneration =====

    boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();

            String logEntry = perm.getCard().getName() + " regenerates.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Exile (used by CombatService) =====

    void resolveExileTopCardsRepeatOnDuplicate(GameData gameData, Permanent creature, UUID targetPlayerId, ExileTopCardsRepeatOnDuplicateEffect effect) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> exiled = gameData.playerExiledCards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String creatureName = creature.getCard().getName();

        String triggerLog = creatureName + "'s ability triggers — " + playerName + " exiles cards from the top of their library.";
        logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                logAndBroadcast(gameData, logEntry);
                break;
            }

            int cardsToExile = Math.min(effect.count(), deck.size());
            List<Card> exiledThisRound = new ArrayList<>();
            for (int i = 0; i < cardsToExile; i++) {
                Card card = deck.removeFirst();
                exiled.add(card);
                exiledThisRound.add(card);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(playerName).append(" exiles ");
            for (int i = 0; i < exiledThisRound.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(exiledThisRound.get(i).getName());
            }
            sb.append(".");
            logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }
}
