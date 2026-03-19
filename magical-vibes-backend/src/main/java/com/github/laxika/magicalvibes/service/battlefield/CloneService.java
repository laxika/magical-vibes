package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloneService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final LegendRuleService legendRuleService;
    private final BattlefieldEntryService battlefieldEntryService;

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride,
                               Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        Card target = targetPerm.getCard();
        Card copy = new Card();
        copy.setName(target.getName());
        copy.setType(target.getType());
        copy.setAdditionalTypes(target.getAdditionalTypes());
        copy.setManaCost(target.getManaCost());
        copy.setColor(target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(powerOverride != null ? powerOverride : target.getPower());
        copy.setToughness(toughnessOverride != null ? toughnessOverride : target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        boolean hasPTOverride = powerOverride != null || toughnessOverride != null;
        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration reg : target.getEffectRegistrations(slot)) {
                // CR 707.9d: when a copy effect provides specific P/T values,
                // characteristic-defining abilities that define P/T are not copied
                if (hasPTOverride && reg.effect().isPowerToughnessDefining()) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }

        // Apply additional types override (e.g., Phyrexian Metamorph is always an artifact)
        if (additionalTypesOverride != null && !additionalTypesOverride.isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(copy.getAdditionalTypes());
            for (CardType overrideType : additionalTypesOverride) {
                if (overrideType != copy.getType() && !merged.contains(overrideType)) {
                    merged.add(overrideType);
                }
            }
            copy.setAdditionalTypes(merged);
        }

        clonePerm.setCard(copy);
    }

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride, Integer toughnessOverride) {
        applyCloneCopy(clonePerm, targetPerm, powerOverride, toughnessOverride, Set.of());
    }

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetId) {
        CopyPermanentOnEnterEffect copyEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect == null) return false;

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (gameQueryService.matchesPermanentPredicate(gameData, p, copyEffect.filter())) {
                validIds.add(p.getId());
            }
        });

        if (validIds.isEmpty()) return false;

        gameData.cloneOperation.card = card;
        gameData.cloneOperation.controllerId = controllerId;
        gameData.cloneOperation.etbTargetId = targetId;
        gameData.cloneOperation.powerOverride = copyEffect.powerOverride();
        gameData.cloneOperation.toughnessOverride = copyEffect.toughnessOverride();
        gameData.cloneOperation.additionalTypesOverride = copyEffect.additionalTypesOverride();
        gameData.cloneOperation.additionalActivatedAbilities = copyEffect.additionalActivatedAbilities();
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CloneCopy());

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                card,
                controllerId,
                List.of(copyEffect),
                card.getName() + " — You may have it enter as a copy of any " + copyEffect.typeLabel() + " on the battlefield."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    public void completeCloneEntry(GameData gameData, UUID targetId) {
        Card card = gameData.cloneOperation.card;
        UUID controllerId = gameData.cloneOperation.controllerId;
        UUID etbTargetId = gameData.cloneOperation.etbTargetId;
        Integer powerOverride = gameData.cloneOperation.powerOverride;
        Integer toughnessOverride = gameData.cloneOperation.toughnessOverride;
        Set<CardType> additionalTypesOverride = gameData.cloneOperation.additionalTypesOverride;
        List<ActivatedAbility> additionalActivatedAbilities = gameData.cloneOperation.additionalActivatedAbilities;

        gameData.cloneOperation.card = null;
        gameData.cloneOperation.controllerId = null;
        gameData.cloneOperation.etbTargetId = null;
        gameData.cloneOperation.powerOverride = null;
        gameData.cloneOperation.toughnessOverride = null;
        gameData.cloneOperation.additionalTypesOverride = Set.of();
        gameData.cloneOperation.additionalActivatedAbilities = List.of();

        Permanent perm = new Permanent(card);

        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm != null) {
                applyCloneCopy(perm, targetPerm, powerOverride, toughnessOverride, additionalTypesOverride);
                // "except it has..." — add additional abilities to the copy (e.g. Evil Twin)
                for (ActivatedAbility extraAbility : additionalActivatedAbilities) {
                    perm.getCard().addActivatedAbility(extraAbility);
                }
            }
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String originalName = card.getName();
        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            String targetName = targetPerm != null ? targetPerm.getCard().getName() : perm.getCard().getName();
            String logEntry = originalName + " enters the battlefield as a copy of " + targetName + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} enters as copy of {} for {}", gameData.id, originalName, targetName, playerName);
        } else {
            String logEntry = originalName + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} enters battlefield without copying for {}", gameData.id, originalName, playerName);
        }

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, perm.getCard(), etbTargetId, true);

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }
}
