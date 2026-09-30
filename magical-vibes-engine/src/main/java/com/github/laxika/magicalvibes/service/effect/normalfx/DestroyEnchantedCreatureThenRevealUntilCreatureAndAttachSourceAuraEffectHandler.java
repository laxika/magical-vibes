package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
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

/** Resolves Shifting Shadow's destruction, reveal, and reattachment sequence. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = findSourceAura(gameData, entry);
        Permanent enchanted = findEnchantedPermanent(gameData, aura);
        UUID enchantedControllerId = entry.getTargetId();
        if (enchantedControllerId == null && enchanted != null) {
            enchantedControllerId = gameQueryService.findPermanentController(gameData, enchanted.getId());
        }

        if (enchanted != null) {
            String sourceName = entry.getCard() == null ? "Shifting Shadow" : entry.getCard().getName();
            destructionSupport.tryDestroyAndLog(gameData, enchanted, sourceName, false, false);
        }
        if (enchantedControllerId == null) {
            return;
        }

        Set<UUID> battlefieldBeforeReveal = new HashSet<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(enchantedControllerId, List.of())) {
            battlefieldBeforeReveal.add(permanent.getId());
        }

        StackEntry revealEntry = new StackEntry(entry);
        revealEntry.setControllerId(enchantedControllerId);
        revealHandler.resolve(gameData, revealEntry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.BATTLEFIELD));

        Permanent revealedCreature = gameData.playerBattlefields
                .getOrDefault(enchantedControllerId, List.of()).stream()
                .filter(permanent -> !battlefieldBeforeReveal.contains(permanent.getId()))
                .findFirst()
                .orElse(null);
        attachAura(gameData, aura, revealedCreature);
    }

    private Permanent findSourceAura(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    private Permanent findEnchantedPermanent(GameData gameData, Permanent aura) {
        if (aura == null || !aura.isAttached()) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
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
