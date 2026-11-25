package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashForControlOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ClashForControlOfEnchantedCreatureEffect} (Captivating Glance): clashes with an
 * opponent for the aura's controller, then gives control of the enchanted creature to the winner —
 * the controller on a win, otherwise the clash opponent.
 */
@Component
@RequiredArgsConstructor
public class ClashForControlOfEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final ClashSupport clashSupport;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ClashForControlOfEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var clash = (ClashForControlOfEnchantedCreatureEffect) effect;
        if (clash.recipientId() != null) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, clash.enchantedId());
            if (enchanted != null) {
                creatureControlService.applyControlEffect(gameData, clash.recipientId(), enchanted,
                        new GainControlOfTargetEffect(Duration.PERMANENT),
                        Duration.PERMANENT, null, entry.getCard().getName());
            }
            return;
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        UUID enchantedId = aura == null ? null : aura.getAttachedTo();

        UUID controllerId = entry.getControllerId();
        UUID opponentId;
        if (clash.opponent() != null) {
            opponentId = subjectResolutionService.player(gameData, entry, effect, clash.opponent());
        } else if (ChoosePlayerEffectHandler.opponentToBeChosenFirst(gameData, entry,
                new ClashForControlOfEnchantedCreatureEffect(null, null,
                        Subjects.playersThisWay(StepKind.CHOOSE)))) {
            return;
        } else {
            opponentId = ChoosePlayerEffectHandler.onlyOpponent(gameData, controllerId);
        }

        boolean won = clashSupport.performClash(gameData, controllerId, opponentId);

        UUID newControllerId = won ? controllerId : opponentId;
        java.util.List<CardEffect> followUps = new java.util.ArrayList<>();
        followUps.add(new com.github.laxika.magicalvibes.model.effect.PlaceClashCardsEffect(
                PlaceClashCardsEffectHandler.apnapOrder(gameData, controllerId, opponentId)));
        if (newControllerId != null && enchantedId != null) {
            followUps.add(new ClashForControlOfEnchantedCreatureEffect(enchantedId, newControllerId, null));
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
    }
}
