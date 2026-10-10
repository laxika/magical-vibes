package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID targetCardId = entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !targetCard.hasType(CardType.CREATURE)) {
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
        exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        if (source != null) {
            Card originalCard = source.getOriginalCard();
            ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect copyEffect =
                    (ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect) effect;
            // "except it has this ability" keeps only this ability itself, wherever the permanent got it
            // (printed, or copied from a Doppelganger), and drops abilities from earlier copies. Read
            // from the current card before the copy replaces it.
            Card retainedSource = source.getCard();
            var retainedAbilities = retainedSource.getActivatedAbilities().stream()
                    .filter(ability -> ability.getEffects().stream().anyMatch(
                            ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect.class::isInstance))
                    .toList();
            permanentCopierService.applyCloneCopy(source, targetCard, null, null, Set.of(),
                    retainedAbilities);
            if (copyEffect.retainedEffectSlot() != null) {
                for (EffectRegistration registration : retainedSource.getEffectRegistrations(
                        copyEffect.retainedEffectSlot())) {
                    source.getCard().addEffect(copyEffect.retainedEffectSlot(),
                            registration.effect(), registration.triggerMode());
                }
            }
            gameLogService.append(gameData,
                    GameLog.textCardText(originalCard.getName() + " exiles ", targetCard,
                            " and becomes a copy of it."));
        } else {
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", targetCard, "."));
        }
    }
}
