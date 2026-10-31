package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final CastingPermissionService castingPermissionService;
    private final CastingCostService castingCostService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SplitCardCastSupport splitCardCastSupport;

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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} library empty for Omen Machine trigger", gameData.id, playerName);
            return;
        }

        // Exile the top card
        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, targetPlayerId, topCard);

        
        gameLogService.append(gameData, GameLog.builder().text(playerName + " exiles ").card(topCard).text(" (" + sourceName + ").").build());
        log.info("Game {} - {} exiles {} (Omen Machine)", gameData.id, playerName, topCard.getName());

        if (topCard.hasType(CardType.LAND)) {
            // Land — put onto the battlefield
            gameData.removeFromExile(topCard.getId());
            Permanent permanent = new Permanent(topCard);
            permanent.setEnteredFromExile(true);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);

            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , topCard, " onto the battlefield."));
            log.info("Game {} - {} puts {} onto battlefield (Omen Machine)", gameData.id, playerName, topCard.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, targetPlayerId, topCard, null, false);
        } else if (SplitCardCastSupport.isSplitCard(topCard)) {
            if (!splitCardCastSupport.beginFromExile(gameData, targetPlayerId, topCard, false,
                    SplitCardCast.ExileCast.ofCard(), Integer.MAX_VALUE, null, null)) {
                gameLogService.append(gameData, GameLog.cardThen(topCard,
                        " has no half that can be cast and remains in exile."));
            }
        } else {
            // Non-land — cast without paying mana cost if able
            if (!castingPermissionService.isSpellCastingAllowed(gameData, targetPlayerId, topCard)
                    || castingCostService.getCastCostModifier(gameData, targetPlayerId, topCard) > 0
                    || !castingCostService.canPayAdditionalSpellCosts(gameData, targetPlayerId, topCard)) {
                gameLogService.append(gameData, GameLog.cardThen(topCard, " can't be cast and remains in exile."));
                log.info("Game {} - {} can't be cast (Omen Machine), stays in exile", gameData.id, topCard.getName());
                return;
            }
            gameData.removeFromExile(topCard.getId());

            StackEntryType spellType = exileSupport.mapCardTypeToSpellType(topCard);
            List<CardEffect> spellEffects = new ArrayList<>(topCard.getCastingCostsAndSpellEffects());

            if (EffectResolution.needsTarget(topCard)) {
                // Targeted spell — need to choose a target
                List<UUID> validTargets = exileCastTargetSupport.firstSlotCandidates(gameData, topCard, spellEffects, targetPlayerId);

                if (validTargets.isEmpty()) {
                    // Can't cast — card stays in exile
                    exileService.exileCard(gameData, targetPlayerId, topCard);
                    gameLogService.append(gameData, GameLog.cardThen(topCard, " has no valid targets and remains in exile."));
                    log.info("Game {} - {} can't be cast (no targets), stays in exile", gameData.id, topCard.getName());
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ExileCastSpellTarget(topCard, targetPlayerId, spellEffects, spellType));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, validTargets,
                        "Choose a target for " + topCard.getName() + ".");

                
                gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , topCard, " without paying its mana cost — choosing target."));
                log.info("Game {} - {} casts {} (Omen Machine), choosing target", gameData.id, playerName, topCard.getName());
            } else {
                // Non-targeted spell — put directly on stack
                gameData.stack.add(new StackEntry(
                        spellType, topCard, targetPlayerId, topCard.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(targetPlayerId, topCard);
                gameData.priorityPassedBy.clear();

                gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , topCard, " without paying its mana cost."));
                log.info("Game {} - {} casts {} (Omen Machine) without paying mana", gameData.id, playerName, topCard.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, topCard, targetPlayerId, false);
            }
        }
    }
}
