package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasFromGraveyardAttachedToCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnAurasFromGraveyardAttachedToCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAurasFromGraveyardAttachedToCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> eligible = gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(Card::isAura)
                .filter(card -> !legalCreatureIds(gameData, controllerId, card).isEmpty())
                .toList();
        if (eligible.isEmpty()) {
            return;
        }

        playerInputService.beginReturnAurasFromGraveyardChoice(gameData,
                new PendingInteraction.ReturnAurasFromGraveyardChoice(controllerId, eligible));
    }

    public void completeCardChoice(GameData gameData, List<UUID> chosenCardIds,
                                   PendingInteraction.ReturnAurasFromGraveyardChoice interaction) {
        Set<UUID> chosen = new HashSet<>(chosenCardIds);
        List<UUID> selected = interaction.validCardIds().stream()
                .filter(chosen::contains)
                .toList();
        continueWithNextAura(gameData, interaction.playerId(), selected, List.of(), List.of());
    }

    public void completeCreatureChoice(GameData gameData, UUID targetId,
                                       PermanentChoiceContext.AttachReturnedAuraToCreature context) {
        Card auraCard = findGraveyardCard(gameData, context.controllerId(), context.auraCardId());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        List<UUID> placedAuras = new ArrayList<>(context.placedAuraCardIds());
        List<UUID> placedCreatures = new ArrayList<>(context.placedCreatureIds());
        if (auraCard != null && target != null
                && legalCreatureIds(gameData, context.controllerId(), auraCard).contains(targetId)) {
            placedAuras.add(auraCard.getId());
            placedCreatures.add(target.getId());
        }
        continueWithNextAura(gameData, context.controllerId(), context.remainingAuraCardIds(),
                placedAuras, placedCreatures);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    /**
     * Chooses a creature for each selected Aura against the battlefield as it is now, then returns
     * them all at once: an Aura that gives protection must not stop a later one from being attached.
     */
    private void continueWithNextAura(GameData gameData, UUID controllerId, List<UUID> auraCardIds,
                                      List<UUID> placedAuraCardIds, List<UUID> placedCreatureIds) {
        List<UUID> placedAuras = new ArrayList<>(placedAuraCardIds);
        List<UUID> placedCreatures = new ArrayList<>(placedCreatureIds);
        for (int i = 0; i < auraCardIds.size(); i++) {
            UUID auraCardId = auraCardIds.get(i);
            Card auraCard = findGraveyardCard(gameData, controllerId, auraCardId);
            if (auraCard == null) {
                continue;
            }

            List<UUID> targetIds = legalCreatureIds(gameData, controllerId, auraCard);
            if (targetIds.isEmpty()) {
                continue;
            }

            List<UUID> remaining = new ArrayList<>(auraCardIds.subList(i + 1, auraCardIds.size()));
            if (targetIds.size() == 1) {
                placedAuras.add(auraCardId);
                placedCreatures.add(targetIds.getFirst());
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachReturnedAuraToCreature(
                            controllerId, auraCardId, remaining, placedAuras, placedCreatures));
            playerInputService.beginPermanentChoice(gameData, controllerId, targetIds,
                    "Choose a creature you control to attach " + auraCard.getName() + " to.");
            return;
        }
        for (int i = 0; i < placedAuras.size(); i++) {
            Card auraCard = findGraveyardCard(gameData, controllerId, placedAuras.get(i));
            Permanent target = gameQueryService.findPermanentById(gameData, placedCreatures.get(i));
            if (auraCard != null) {
                returnAuraAttachedTo(gameData, controllerId, auraCard, target);
            }
        }
    }

    private void returnAuraAttachedTo(GameData gameData, UUID controllerId, Card auraCard,
                                      Permanent target) {
        if (target == null) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCard.getId());

        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(target.getId());
        aura.setExileIfLeavesBattlefield(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, aura);

        if (gameQueryService.findPermanentById(gameData, aura.getId()) == null) {
            return;
        }
        // One delayed trigger of the returning ability exiles all of its Auras at its controller's next end step.
        StackEntry resolvingEntry = gameData.pendingEffectResolutionEntry;
        Card delayedTriggerSource = resolvingEntry != null && resolvingEntry.getCard() != null
                ? resolvingEntry.getCard() : auraCard;
        gameData.queueDelayedAction(new DelayedPermanentAction(
                aura.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP, false, null,
                controllerId, null, false, delayedTriggerSource));
        creatureControlService.reconcileControl(gameData);
        gameLogService.append(gameData, GameLog.builder()
                .card(auraCard)
                .text(" enters the battlefield attached to ")
                .card(target.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} returns from graveyard attached to {}", gameData.id,
                auraCard.getName(), target.getCard().getName());
    }

    private List<UUID> legalCreatureIds(GameData gameData, UUID controllerId, Card auraCard) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && auraAttachmentService.canEnchant(gameData, auraCard, controllerId, permanent)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    private Card findGraveyardCard(GameData gameData, UUID controllerId, UUID cardId) {
        return gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
