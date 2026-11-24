package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureIsCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
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
 * before the layer pass, recorded as a {@link com.github.laxika.magicalvibes.model.effect.ResolvedCopyEffect}
 * floating effect whose end the duration clock undoes (the newest copy effect left applies again, CR 613.7).
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
        boolean needsChoice = card.getStaticEffects().stream()
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
                // A permanent that can't be chosen is not offered (CR 608.2d).
                if (gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeChosen(gameData, permanent)) {
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
        boolean isCopyAura = aura.getCard().getStaticEffects().stream()
                .anyMatch(EnchantedCreatureIsCopyOfChosenCreatureEffect.class::isInstance);
        if (!isCopyAura || !aura.isAttached() || aura.getChosenPermanentId() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        Permanent chosen = gameQueryService.findPermanentById(gameData, aura.getChosenPermanentId());
        if (enchanted == null || chosen == null) {
            return;
        }

        // The chosen creature's copiable values are locked in now, even when it is the enchanted
        // creature itself and already carries an earlier copy effect (Metamorphic Alteration rulings).
        applyWhileAttachedCopy(gameData, aura, enchanted, chosen, Duration.WHILE_ATTACHED);
    }

    private void applyWhileAttachedCopy(GameData gameData, Permanent source, Permanent affected,
                                        Permanent chosen, Duration duration) {
        Card originalCard = affected.getCard();
        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, affected);
        permanentCopierService.applyCloneCopy(affected, chosen, null, null);
        beginCopy(gameData, source, affected, preCopyCard, duration);

        // Cards, not names: the log shows no card of a face-down permanent (CR 708.2).
        gameLogService.append(gameData,
                GameLog.cardTextCard(originalCard, " becomes a copy of ", chosen.getCard(), "."));
        log.info("Game {} - {} becomes a copy of {} ({})", gameData.id, originalCard.getName(),
                chosen.getCard().getName(), source.getCard().getName());
    }

    /**
     * Makes {@code attached} a copy of {@code exiledCreature} for {@code duration}: while the Equipment or Aura stays
     * attached as a static ability or rule keeps it ({@code WHILE_ATTACHED}), or "for as long as this Equipment remains
     * attached to it" from a resolution ({@code WHILE_REMAINS_ATTACHED}).
     */
    public void applyExiledCreatureCopy(GameData gameData, Permanent equipment, Permanent attached,
                                         Card exiledCreature, Duration duration) {
        Card originalCard = attached.getCard();
        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, attached);
        permanentCopierService.applyCloneCopy(attached, exiledCreature, null, null, java.util.Set.of());
        beginCopy(gameData, equipment, attached, preCopyCard, duration);
        gameLogService.append(gameData,
                GameLog.cardThen(originalCard, " becomes a copy of " + exiledCreature.getName() + "."));
        log.info("Game {} - {} becomes a copy of {} ({})", gameData.id, originalCard.getName(),
                exiledCreature.getName(), equipment.getCard().getName());
    }

    /**
     * Records the copy just applied to {@code affected} as a layer-1 floating effect of {@code source} for
     * {@code duration}; as it ends, the duration clock has the newest copy effect left apply again, or the card shown
     * before them all (CR 613.7).
     */
    private void beginCopy(GameData gameData, Permanent source, Permanent affected, Card preCopyCard,
                           Duration duration) {
        PermanentCopierService.beginCopy(gameData, FaceDownVisibility.publicName(gameData, source), source.getId(),
                gameQueryService.findPermanentController(gameData, source.getId()), affected, preCopyCard, duration);
    }
}
