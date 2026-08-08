package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureIsCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "As this Aura enters, choose a creature. Enchanted creature is a copy of the chosen creature."
 * (Metamorphic Alteration).
 *
 * <p>Owns every {@code instanceof} check for {@link ChooseCreatureOnEnterEffect} and
 * {@link EnchantedCreatureIsCopyOfChosenCreatureEffect} so the entry, choice and removal call sites
 * stay free of concrete-effect knowledge. The copy itself follows the layer-1 model documented in
 * {@code agent-docs/LAYER_SYSTEM.md}: a card swap through {@link PermanentCopierService} applied
 * before the layer pass, paired with a {@code WHILE_ATTACHED} floating effect whose expiry drives
 * the revert.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuraCopyService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;
    private final PlayerInputService playerInputService;

    /**
     * Begins the "as this enters, choose a creature" choice for a permanent that just entered, if
     * its card has one and at least one creature is on the battlefield.
     *
     * @return {@code true} when a choice was begun and the caller must yield to player input
     */
    public boolean beginChooseCreatureOnEnter(GameData gameData, UUID controllerId, Permanent entered,
                                              Card card, UUID targetId, boolean wasCastFromHand) {
        boolean needsChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(ChooseCreatureOnEnterEffect.class::isInstance);
        if (!needsChoice) {
            return false;
        }

        List<UUID> validIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    validIds.add(permanent.getId());
                }
            }
        }
        if (validIds.isEmpty()) {
            return false;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChooseCreatureAsEnter(
                entered.getId(), controllerId, card, targetId, wasCastFromHand, 0, false));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, "Choose a creature.");
        return true;
    }

    /**
     * Applies "enchanted creature is a copy of the chosen creature" once the Aura is attached and
     * its chosen creature is known. No-op for permanents without the effect.
     */
    public void applyChosenCreatureCopy(GameData gameData, Permanent aura) {
        boolean isCopyAura = aura.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(EnchantedCreatureIsCopyOfChosenCreatureEffect.class::isInstance);
        if (!isCopyAura || !aura.isAttached() || aura.getChosenPermanentId() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        Permanent chosen = gameQueryService.findPermanentById(gameData, aura.getChosenPermanentId());
        if (enchanted == null || chosen == null || enchanted.getId().equals(chosen.getId())) {
            return;
        }

        String originalName = enchanted.getCard().getName();
        if (!enchanted.isCopyWhileAttached()) {
            enchanted.setWhileAttachedPreCopyCard(enchanted.getCard());
        }
        permanentCopierService.applyCloneCopy(enchanted, chosen, null, null);
        enchanted.setCopyWhileAttached(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), aura.getCard().getName(), aura.getId(),
                gameQueryService.findPermanentController(gameData, aura.getId()),
                new EnchantedCreatureIsCopyOfChosenCreatureEffect(), enchanted.getId(), null, null,
                EffectDuration.WHILE_ATTACHED, 0));

        gameLogService.append(gameData, GameLog.text(
                originalName + " becomes a copy of " + chosen.getCard().getName() + "."));
        log.info("Game {} - {} becomes a copy of {} ({})", gameData.id, originalName,
                chosen.getCard().getName(), aura.getCard().getName());
    }

    /**
     * Reverts every "while attached" copy carried by the given expired floating effects. Called
     * when an Aura leaves the battlefield or becomes unattached.
     */
    public void revertExpiredCopies(GameData gameData, List<FloatingContinuousEffect> expired) {
        for (FloatingContinuousEffect floating : expired) {
            if (!(floating.effect() instanceof EnchantedCreatureIsCopyOfChosenCreatureEffect)) {
                continue;
            }
            Permanent enchanted = gameQueryService.findPermanentById(gameData, floating.affectedPermanentId());
            if (enchanted == null || !enchanted.isCopyWhileAttached()) {
                continue;
            }
            String copyName = enchanted.getCard().getName();
            enchanted.revertWhileAttachedCopy();
            gameLogService.append(gameData, GameLog.text(
                    copyName + " is no longer a copy and reverts to " + enchanted.getCard().getName() + "."));
            log.info("Game {} - {} reverts from copy back to {} ({} left)", gameData.id, copyName,
                    enchanted.getCard().getName(), floating.sourceCardName());
        }
    }
}
