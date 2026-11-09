package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Shifting Shadow's granted ability: destroys the creature, reveals, and attaches the Aura that granted it. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "This creature" is the ability's source, "Shifting Shadow" the Aura that granted it, each as the ability
        // resolves: one that has left is neither destroyed nor attached (the card's rulings).
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.requireGrantingPermanentId());
        Permanent creature = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID controllerId = entry.getControllerId();

        if (creature != null) {
            destructionSupport.tryDestroyAndLog(gameData, creature, entry.getCard().getName(), false, false);
        }

        Set<UUID> battlefieldBeforeReveal = new HashSet<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            battlefieldBeforeReveal.add(permanent.getId());
        }

        revealHandler.resolve(gameData, entry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.BATTLEFIELD));

        Permanent revealedCreature = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> !battlefieldBeforeReveal.contains(permanent.getId()))
                .findFirst()
                .orElse(null);
        attachAura(gameData, aura, revealedCreature);
    }

    private void attachAura(GameData gameData, Permanent aura, Permanent target) {
        if (aura == null || target == null) {
            return;
        }
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId == null
                || !auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, target)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(target.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData,
                GameLog.cardTextCard(aura.getCard(), " is now attached to ", target.getCard(), "."));
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, target.getId());
        log.info("Game {} - {} attached to {}", gameData.id, aura.getCard().getName(), target.getCard().getName());
    }
}
