package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OmenMachineDrawStepEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OmenMachineDrawStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} library empty for Omen Machine trigger", gameData.id, playerName);
            return;
        }

        // Exile the top card
        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, targetPlayerId, topCard);

        String exileLog = playerName + " exiles " + topCard.getName() + " (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiles {} (Omen Machine)", gameData.id, playerName, topCard.getName());

        if (topCard.hasType(CardType.LAND)) {
            // Land — put onto the battlefield
            gameData.removeFromExile(topCard.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, new Permanent(topCard));

            String landLog = playerName + " puts " + topCard.getName() + " onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, landLog);
            log.info("Game {} - {} puts {} onto battlefield (Omen Machine)", gameData.id, playerName, topCard.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, targetPlayerId, topCard, null, false);
        } else {
            // Non-land — cast without paying mana cost if able
            gameData.removeFromExile(topCard.getId());

            StackEntryType spellType = exileSupport.mapCardTypeToSpellType(topCard);
            List<CardEffect> spellEffects = new ArrayList<>(topCard.getEffects(EffectSlot.SPELL));

            if (EffectResolution.needsTarget(topCard)) {
                // Targeted spell — need to choose a target
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(topCard);
                List<UUID> validTargets = new ArrayList<>();

                // Only add permanents if the spell can actually target them
                if (allowedTargets.contains(TargetType.PERMANENT)) {
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (topCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
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
                    // Can't cast — card stays in exile
                    exileService.exileCard(gameData, targetPlayerId, topCard);
                    String noTargetLog = topCard.getName() + " has no valid targets and remains in exile.";
                    gameBroadcastService.logAndBroadcast(gameData, noTargetLog);
                    log.info("Game {} - {} can't be cast (no targets), stays in exile", gameData.id, topCard.getName());
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ExileCastSpellTarget(topCard, targetPlayerId, spellEffects, spellType));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, validTargets,
                        "Choose a target for " + topCard.getName() + ".");

                String castLog = playerName + " casts " + topCard.getName() + " without paying its mana cost — choosing target.";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Omen Machine), choosing target", gameData.id, playerName, topCard.getName());
            } else {
                // Non-targeted spell — put directly on stack
                gameData.stack.add(new StackEntry(
                        spellType, topCard, targetPlayerId, topCard.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(targetPlayerId, topCard);
                gameData.priorityPassedBy.clear();

                String castLog = playerName + " casts " + topCard.getName() + " without paying its mana cost.";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Omen Machine) without paying mana", gameData.id, playerName, topCard.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, topCard, targetPlayerId, false);
            }
        }
    }
}
