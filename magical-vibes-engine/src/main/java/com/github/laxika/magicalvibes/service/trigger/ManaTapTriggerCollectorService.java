package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenCreatureTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Trigger collectors for controller-scoped creature-mana events. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManaTapTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = AddManaWhenCreatureTappedForManaEffect.class,
            slot = EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA)
    private boolean handleAddManaWhenCreatureTappedForMana(TriggerMatchContext match,
            AddManaWhenCreatureTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.CreatureTapForMana creatureTap = (TriggerContext.CreatureTapForMana) ctx;
        match.gameData().playerManaPools.get(creatureTap.tappingPlayerId()).add(trigger.color());
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(creatureTap.tappingPlayerId())
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        log.info("Game {} - {} triggers on creature tap for mana", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = AddManaOfTypeProducedByTappedPermanentEffect.class,
            slot = EffectSlot.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA)
    private boolean handleAddManaOfTypeProducedByNonlandPermanent(TriggerMatchContext match,
                                                                  AddManaOfTypeProducedByTappedPermanentEffect trigger,
                                                                  TriggerContext ctx) {
        TriggerContext.NonlandPermanentTapForMana nonlandTap =
                (TriggerContext.NonlandPermanentTapForMana) ctx;
        if (nonlandTap.producedManaTypes() == null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of((CardEffect) trigger)),
                    null,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            match.gameData().pendingManaAbilityTriggers.add(entry);
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        if (nonlandTap.producedManaTypes().isEmpty()) {
            return false;
        }

        if (trigger.requiredColor() != null
                && !nonlandTap.producedManaTypes().contains(trigger.requiredColor())) {
            return false;
        }

        ManaColor manaColor = trigger.requiredColor() != null
                ? trigger.requiredColor()
                : nonlandTap.producedManaTypes().iterator().next();
        match.gameData().playerManaPools.get(match.controllerId()).add(manaColor);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(match.controllerId())
                        + " adds 1 additional " + manaColor.name().toLowerCase() + " mana."));
        log.info("Game {} - {} triggers on nonland permanent tap for mana", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = AddManaOfTypeProducedByTappedPermanentEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA)
    private boolean handleAddManaOfTypeProducedByAnyPermanent(TriggerMatchContext match,
                                                               AddManaOfTypeProducedByTappedPermanentEffect trigger,
                                                               TriggerContext ctx) {
        TriggerContext.PermanentTapForMana permanentTap = (TriggerContext.PermanentTapForMana) ctx;
        Card sourceCard = match.sourceCard() != null
                ? match.sourceCard()
                : match.permanent().getCard();
        if (permanentTap.producedManaTypes() == null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    permanentTap.tappingPlayerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of((CardEffect) trigger)),
                    null,
                    match.permanent() == null ? null : match.permanent().getId());
            entry.setNonTargeting(true);
            if (match.sourcePlanarObject() != null) {
                entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
            }
            match.gameData().pendingManaAbilityTriggers.add(entry);
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            return true;
        }
        if (permanentTap.producedManaTypes().isEmpty()) {
            return false;
        }

        ManaColor manaColor = permanentTap.producedManaTypes().iterator().next();
        match.gameData().playerManaPools.get(permanentTap.tappingPlayerId()).add(manaColor);
        gameLogService.append(match.gameData(), GameLog.cardThen(sourceCard,
                " triggers — " + match.gameData().playerIdToName.get(permanentTap.tappingPlayerId())
                        + " adds 1 additional " + manaColor.name().toLowerCase() + " mana."));
        log.info("Game {} - {} triggers on permanent tap for mana", match.gameData().id,
                sourceCard.getName());
        return true;
    }
}
