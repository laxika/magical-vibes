package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraReanimateFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BecomeAuraReanimateFromGraveyardEffect}: intervening-if the source is still on
 * the battlefield, it becomes an Aura, the targeted creature card is put onto the battlefield under
 * the controller, and the source attaches to it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeAuraReanimateFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final AuraAttachmentService auraAttachmentService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeAuraReanimateFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = findSource(gameData, entry);
        if (source == null) {
            // Intervening-if "if it's on the battlefield" failed at resolution.
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (it's no longer on the battlefield)."));
            log.info("Game {} - {} become-aura reanimate skipped (source left battlefield)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            return;
        }

        becomeAura(source);

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (graveyardCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (target creature card is no longer in a graveyard)."));
            log.info("Game {} - {} become-aura reanimate fizzles (target left graveyard)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent creature = graveyardReturnSupport.reanimateTargetedCard(
                gameData, entry.getControllerId(), graveyardCard);
        if (creature == null) {
            // Blocked from entering (e.g. Grafdigger's Cage): source stays an unattached Aura and
            // will be swept by SBAs.
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " becomes an Aura but the creature could not enter the battlefield."));
            log.info("Game {} - {} became Aura but reanimation was blocked",
                    gameData.id, entry.getCard().getName());
            return;
        }

        if (!auraAttachmentService.canEnchant(
                gameData, source.getCard(), entry.getControllerId(), creature)) {
            destructionSupport.sacrificeAndLog(gameData, creature, entry.getControllerId());
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        source.setAttachedTo(creature.getId());
        source.setChosenPermanentId(creature.getId());
        source.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" becomes an Aura attached to ")
                .card(creature.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} becomes Aura attached to {}",
                gameData.id, source.getCard().getName(), creature.getCard().getName());
    }

    private Permanent findSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        UUID cardId = entry.getCard().getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(cardId)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Turns the source into an Aura with "enchant creature" via a runtime card copy (cards freeze
     * once live, so the printed face cannot be mutated in place).
     */
    private void becomeAura(Permanent source) {
        if (source.getCard().isAura()) {
            return;
        }
        Card copy = source.getCard().createRuntimeCopy();
        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        if (!subtypes.contains(CardSubtype.AURA)) {
            subtypes.add(CardSubtype.AURA);
        }
        copy.setSubtypes(subtypes);
        if (copy.getTargetFilter() == null) {
            copy.target(TargetFilters.creature());
        }
        copy.freeze();
        source.setCard(copy);
    }
}
