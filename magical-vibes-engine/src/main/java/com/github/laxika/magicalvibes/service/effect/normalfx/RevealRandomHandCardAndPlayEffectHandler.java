package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomHandCardAndPlayEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final SessionManager sessionManager;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomHandCardAndPlayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards in hand (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} trigger: {} has no cards in hand", gameData.id, sourceName, playerName);
            return;
        }

        // Reveal a card at random from hand
        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);

        String revealLog = playerName + " reveals " + revealed.getName() + " at random (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " reveals ").card(revealed).text(" at random (" + sourceName + ").").build());

        List<CardView> cardViews = List.of(cardViewFactory.create(revealed));
        for (UUID playerId : gameData.orderedPlayerIds) {
            sessionManager.sendToPlayer(playerId, new RevealHandMessage(cardViews, playerName));
        }

        log.info("Game {} - {} trigger: {} reveals {}", gameData.id, sourceName, playerName, revealed.getName());

        if (revealed.hasType(CardType.LAND)) {
            // Land — remove from hand and put onto the battlefield
            hand.remove(randomIndex);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, new Permanent(revealed));

            String landLog = playerName + " puts " + revealed.getName() + " onto the battlefield (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " puts ").card(revealed).text(" onto the battlefield (" + sourceName + ").").build());
            log.info("Game {} - {} puts {} onto battlefield (Wild Evocation)", gameData.id, playerName, revealed.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, targetPlayerId, revealed, null, false);
        } else {
            // Non-land — cast without paying mana cost if able
            StackEntryType spellType = playerInteractionSupport.mapCardTypeToSpellType(revealed);
            List<CardEffect> spellEffects = new ArrayList<>(revealed.getEffects(EffectSlot.SPELL));

            if (EffectResolution.needsTarget(revealed)) {
                // Targeted spell — check for valid targets
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(revealed);
                List<UUID> validTargets = new ArrayList<>();
                if (allowedTargets.contains(TargetType.PERMANENT)) {
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (revealed.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                    validTargets.add(p.getId());
                                }
                            } else if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                }
                if (allowedTargets.contains(TargetType.PLAYER)) {
                    validTargets.addAll(gameData.orderedPlayerIds);
                }

                if (validTargets.isEmpty()) {
                    // Can't cast — card stays in hand
                    String noTargetLog = revealed.getName() + " has no valid targets and stays in " + playerName + "'s hand.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(revealed).text(" has no valid targets and stays in " + playerName + "'s hand.").build());
                    log.info("Game {} - {} can't be cast (no targets), stays in hand", gameData.id, revealed.getName());
                    return;
                }

                // Remove from hand, then prompt for target
                hand.remove(randomIndex);
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.HandCastSpellTarget(revealed, targetPlayerId, spellEffects, spellType));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, validTargets,
                        "Choose a target for " + revealed.getName() + ".");

                String castLog = playerName + " casts " + revealed.getName() + " without paying its mana cost — choosing target (" + sourceName + ").";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " casts ").card(revealed).text(" without paying its mana cost — choosing target (" + sourceName + ").").build());
                log.info("Game {} - {} casts {} (Wild Evocation), choosing target", gameData.id, playerName, revealed.getName());
            } else {
                // Non-targeted spell — remove from hand and put directly on stack
                hand.remove(randomIndex);
                gameData.stack.add(new StackEntry(
                        spellType, revealed, targetPlayerId, revealed.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(targetPlayerId, revealed);
                gameData.priorityPassedBy.clear();

                String castLog = playerName + " casts " + revealed.getName() + " without paying its mana cost (" + sourceName + ").";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " casts ").card(revealed).text(" without paying its mana cost (" + sourceName + ").").build());
                log.info("Game {} - {} casts {} (Wild Evocation) without paying mana", gameData.id, playerName, revealed.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, revealed, targetPlayerId, false);
            }
        }
    
    }
}
