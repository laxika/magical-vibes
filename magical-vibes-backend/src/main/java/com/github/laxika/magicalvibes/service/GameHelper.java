package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class GameHelper {

    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final CardViewFactory cardViewFactory;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final DraftRegistry draftRegistry;
    private final DraftService draftService;

    public GameHelper(SessionManager sessionManager,
                      GameRegistry gameRegistry,
                      CardViewFactory cardViewFactory,
                      GameQueryService gameQueryService,
                      GameBroadcastService gameBroadcastService,
                      PlayerInputService playerInputService,
                      DraftRegistry draftRegistry,
                      DraftService draftService) {
        this.sessionManager = sessionManager;
        this.gameRegistry = gameRegistry;
        this.cardViewFactory = cardViewFactory;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.draftRegistry = draftRegistry;
        this.draftService = draftService;
    }

    // ===== Lifecycle methods =====

    public void addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        if (card.isShufflesIntoLibraryFromGraveyard()) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            Collections.shuffle(deck);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
        } else {
            gameData.playerGraveyards.get(ownerId).add(card);
        }
    }

    public boolean removePermanentToGraveyard(GameData gameData, Permanent target) {
        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                addCardToGraveyard(gameData, graveyardOwnerId, target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
                collectDeathTrigger(gameData, target.getCard(), playerId, wasCreature);
                if (wasCreature) {
                    checkAllyCreatureDeathTriggers(gameData, playerId);
                }
                return true;
            }
        }
        return false;
    }

    public boolean removePermanentToExile(GameData gameData, Permanent target) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.playerExiledCards.get(ownerId).add(target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
                return true;
            }
        }
        return false;
    }

    public void removeOrphanedAuras(GameData gameData) {
        boolean anyRemoved = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.getAttachedTo() != null && gameQueryService.findPermanentById(gameData, p.getAttachedTo()) == null) {
                    if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        // Equipment stays on the battlefield unattached when the equipped creature leaves
                        p.setAttachedTo(null);
                        String logEntry = p.getCard().getName() + " becomes unattached (equipped creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        it.remove();
                        addCardToGraveyard(gameData, playerId, p.getOriginalCard());
                        String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                        anyRemoved = true;
                    }
                }
            }
        }
        if (anyRemoved) {

        }
        returnStolenCreatures(gameData);
    }

    public void removeCardFromGraveyardById(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                return;
            }
        }
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
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
            } else if (effect instanceof BoostTargetCreatureEffect || effect instanceof DealDamageToTargetCreatureEffect) {
                // Targeted death trigger — queue for target selection after current action completes
                gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                        dyingCard, controllerId, new ArrayList<>(List.of(effect))
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

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.pendingDeathTriggerTargets.peekFirst();

            // Collect valid creature targets from all battlefields
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }

            if (validTargets.isEmpty()) {
                // No valid targets — trigger can't go on the stack, skip it
                gameData.pendingDeathTriggerTargets.removeFirst();
                String logEntry = pending.dyingCard().getName() + "'s death trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger skipped (no valid creature targets)",
                        gameData.id, pending.dyingCard().getName());
                continue;
            }

            // Remove from queue and begin permanent choice
            gameData.pendingDeathTriggerTargets.removeFirst();
            gameData.permanentChoiceContext = pending;
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.dyingCard().getName() + "'s ability — Choose target creature.");

            String logEntry = pending.dyingCard().getName() + "'s death trigger — choose a target creature.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
    }

    boolean checkWinCondition(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            if (life <= 0) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

                notifyDraftIfTournamentGame(gameData, winnerId);

                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life);
                return true;
            }
        }
        return false;
    }

    public void declareWinner(GameData gameData, UUID winnerId) {
        String winnerName = gameData.playerIdToName.get(winnerId);

        gameData.status = GameStatus.FINISHED;

        String logEntry = winnerName + " wins the game!";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

        notifyDraftIfTournamentGame(gameData, winnerId);

        gameRegistry.remove(gameData.id);

        log.info("Game {} - {} wins!", gameData.id, winnerName);
    }

    private void notifyDraftIfTournamentGame(GameData gameData, UUID winnerId) {
        if (gameData.draftId != null) {
            DraftData draftData = draftRegistry.get(gameData.draftId);
            if (draftData != null) {
                draftService.handleGameFinished(draftData, winnerId);
            }
        }
    }

    void resetEndOfTurnModifiers(GameData gameData) {
        boolean anyReset = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                        || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()
                        || p.isAnimatedUntilEndOfTurn() || p.isCantRegenerateThisTurn()) {
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

    public void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    it.remove();
                    UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(p.getId(), playerId);
                    gameData.stolenCreatures.remove(p.getId());
                    addCardToGraveyard(gameData, graveyardOwnerId, p.getOriginalCard());
                    collectDeathTrigger(gameData, p.getCard(), playerId, true);
                    checkAllyCreatureDeathTriggers(gameData, playerId);
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                    anyDied = true;
                } else if (p.getCard().getType() == CardType.PLANESWALKER && p.getLoyaltyCounters() <= 0) {
                    it.remove();
                    addCardToGraveyard(gameData, playerId, p.getOriginalCard());
                    String logEntry = p.getCard().getName() + " has no loyalty counters and is put into the graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, p.getCard().getName());
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
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
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
        Card copy = new Card();
        copy.setName(target.getName());
        copy.setType(target.getType());
        copy.setManaCost(target.getManaCost());
        copy.setColor(target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(target.getPower());
        copy.setToughness(target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setNeedsTarget(target.isNeedsTarget());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
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

    public boolean checkLegendRule(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        Map<String, List<UUID>> legendaryByName = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getSupertypes().contains(com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY)) {
                legendaryByName.computeIfAbsent(perm.getCard().getName(), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2) {
                gameData.permanentChoiceContext = new PermanentChoiceContext.LegendRule(entry.getKey());
                playerInputService.beginPermanentChoice(gameData, controllerId, entry.getValue(),
                        "You control multiple legendary permanents named " + entry.getKey() + ". Choose one to keep.");
                return true;
            }
        }
        return false;
    }

    // ===== Clone replacement effect =====

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        boolean needsCopyChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
        if (!needsCopyChoice) return false;

        List<UUID> creatureIds = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) return false;

        gameData.pendingCloneCard = card;
        gameData.pendingCloneControllerId = controllerId;
        gameData.pendingCloneETBTargetId = targetPermanentId;
        gameData.permanentChoiceContext = new PermanentChoiceContext.CloneCopy();

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                card,
                controllerId,
                List.of(new CopyCreatureOnEnterEffect()),
                card.getName() + " — You may have it enter as a copy of any creature on the battlefield."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    public void completeCloneEntry(GameData gameData, UUID targetPermanentId) {
        Card card = gameData.pendingCloneCard;
        UUID controllerId = gameData.pendingCloneControllerId;
        UUID etbTargetId = gameData.pendingCloneETBTargetId;

        gameData.pendingCloneCard = null;
        gameData.pendingCloneControllerId = null;
        gameData.pendingCloneETBTargetId = null;

        Permanent perm = new Permanent(card);

        if (targetPermanentId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (targetPerm != null) {
                applyCloneCopy(perm, targetPerm);
            }
        }

        gameData.playerBattlefields.get(controllerId).add(perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        if (targetPermanentId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermanentId);
            String targetName = targetPerm != null ? targetPerm.getCard().getName() : perm.getCard().getName();
            String logEntry = "Clone enters the battlefield as a copy of " + targetName + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Clone enters as copy of {} for {}", gameData.id, targetName, playerName);
        } else {
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} enters battlefield as 0/0 for {}", gameData.id, card.getName(), playerName);
        }

        handleCreatureEnteredBattlefield(gameData, controllerId, perm.getCard(), etbTargetId);

        if (gameData.awaitingInput == null) {
            checkLegendRule(gameData, controllerId);
        }
    }

    // ===== ETB pipeline =====

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetPermanentId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetPermanentId);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                .filter(e -> !(e instanceof CopyCreatureOnEnterEffect))
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
                        gameBroadcastService.logAndBroadcast(gameData, etbLog);
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
                allCardViews.add(cardViewFactory.create(graveyardCard));
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
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            // Prompt player to choose targets before putting ability on the stack
            int maxTargets = Math.min(exile.maxTargets(), allCardIds.size());
            gameData.pendingGraveyardTargetCard = card;
            gameData.pendingGraveyardTargetControllerId = controllerId;
            gameData.pendingGraveyardTargetEffects = new ArrayList<>(allEffects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from graveyards to exile.");
        }
    }

    void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                       StackEntryType entryType, int xValue) {
        // Collect creature cards from controller's own graveyard
        List<UUID> creatureCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.getType() == CardType.CREATURE) {
                    creatureCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        gameData.pendingGraveyardTargetCard = card;
        gameData.pendingGraveyardTargetControllerId = controllerId;
        gameData.pendingGraveyardTargetEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.pendingGraveyardTargetEntryType = entryType;
        gameData.pendingGraveyardTargetXValue = xValue;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCardIds, cardViews, xValue,
                "Choose " + xValue + " target creature card" + (xValue != 1 ? "s" : "") + " from your graveyard to exile.");
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
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                }
            }
        }
    }

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
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
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                    }
                }
            }
        }
    }

    // ===== Control =====

    public void stealCreature(GameData gameData, UUID newControllerId, Permanent creature) {
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
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null) {
                it.remove();
                gameData.enchantmentDependentStolenCreatures.remove(creatureId);
                continue;
            }

            if (gameQueryService.hasAuraWithEffect(gameData, creature, ControlEnchantedCreatureEffect.class)) {
                continue;
            }

            if (gameData.enchantmentDependentStolenCreatures.contains(creatureId)
                    && gameQueryService.isEnchanted(gameData, creature)) {
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
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        Permanent target = gameQueryService.findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        if (target == null) return damage;

        int effectiveDamage = applyCreaturePreventionShield(gameData, target, damage);
        String logEntry = target.getCard().getName() + " absorbs " + effectiveDamage + " redirected " + sourceName + " damage.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
            } else {
                removePermanentToGraveyard(gameData, target);
                String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
                removeOrphanedAuras(gameData);
            }
        }

        return 0;
    }

    // ===== Triggers =====

    public void checkSpellCastTriggers(GameData gameData, Card spellCard) {
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

        playerInputService.processNextMayAbility(gameData);
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        boolean anyTriggered = false;

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(discardingPlayerId)) continue;

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DISCARDS)) {
                    if (effect instanceof DealDamageToDiscardingPlayerEffect trigger) {
                        String cardName = perm.getCard().getName();
                        int damage = trigger.damage();

                        String logEntry = cardName + " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(discardingPlayerId) + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

                        if (!gameQueryService.isDamageFromSourcePrevented(gameData, perm.getCard().getColor())
                                && !applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, perm.getCard().getColor())) {
                            int effectiveDamage = applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
                            effectiveDamage = redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
                            int currentLife = gameData.playerLifeTotals.getOrDefault(discardingPlayerId, 20);
                            gameData.playerLifeTotals.put(discardingPlayerId, currentLife - effectiveDamage);
                        }

                        anyTriggered = true;
                    }
                }
            }
        }

        if (anyTriggered) {
            checkWinCondition(gameData);
        }

        // Check the discarded card itself for self-discard triggers (e.g. Guerrilla Tactics)
        if (discardedCard != null && gameData.discardCausedByOpponent) {
            List<CardEffect> selfTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT);
            if (!selfTriggers.isEmpty()) {
                gameData.pendingDiscardSelfTriggers.add(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                        discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                ));
                String logEntry = discardedCard.getName() + " was discarded by an opponent's effect — its ability triggers!";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
            }
        }
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId) {
        boolean anyTriggered = false;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)) {
                    if (effect instanceof DealDamageOnLandTapEffect trigger) {
                        String cardName = perm.getCard().getName();
                        int damage = gameQueryService.applyDamageMultiplier(gameData, trigger.damage());

                        String logEntry = cardName + " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(tappingPlayerId) + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} triggers on land tap, dealing {} damage to {}",
                                gameData.id, cardName, damage, gameData.playerIdToName.get(tappingPlayerId));

                        if (!gameQueryService.isDamageFromSourcePrevented(gameData, perm.getCard().getColor())
                                && !applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, perm.getCard().getColor())) {
                            int effectiveDamage = applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
                            effectiveDamage = redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
                            int currentLife = gameData.playerLifeTotals.getOrDefault(tappingPlayerId, 20);
                            gameData.playerLifeTotals.put(tappingPlayerId, currentLife - effectiveDamage);
                        }

                        anyTriggered = true;
                    }
                }
            }
        }

        if (anyTriggered) {
            checkWinCondition(gameData);
        }
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        while (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            PermanentChoiceContext.DiscardTriggerAnyTarget pending = gameData.pendingDiscardSelfTriggers.peekFirst();

            // Collect valid targets: all creatures and planeswalkers on all battlefields + all players
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)
                            || p.getCard().getType() == CardType.PLANESWALKER) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pendingDiscardSelfTriggers.removeFirst();
            gameData.permanentChoiceContext = pending;
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.discardedCard().getName() + "'s ability — Choose any target.");

            String logEntry = pending.discardedCard().getName() + "'s discard trigger — choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.discardedCard().getName());
            return;
        }
    }

    // ===== Draw =====

    public void resolveDrawCard(GameData gameData, UUID playerId) {
        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));
    }

    // ===== Regeneration =====

    boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.isCantRegenerateThisTurn()) {
            return false;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();

            String logEntry = perm.getCard().getName() + " regenerates.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
            gameBroadcastService.logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                gameBroadcastService.logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }
}
