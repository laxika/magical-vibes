package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardHandUnlessPaysLifeEffect}: the target player either pays the life or
 * discards their entire hand. The choice belongs to the target player, so a payable target is
 * prompted via the may-ability system; a target that can't pay the life discards immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardHandUnlessPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DiscardHandEffectHandler discardHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardHandUnlessPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardHandUnlessPaysLifeEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)
                && gameData.getLife(targetPlayerId) >= e.lifeCost();

        if (!canPay) {
            // Can't pay the life — discard the whole hand (carrying the caster as controller so
            // "an opponent made you discard" triggers see it correctly).
            discardTargetHand(gameData, casterId, targetPlayerId, entry.getCard());
            return;
        }

        // Payable — ask the target player. Carry the caster id in the targetCardId slot so the
        // decline branch can attribute the discard to the caster.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, discard your hand. ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt, casterId));
    }

    /** Discard {@code targetPlayerId}'s entire hand, attributed to {@code casterId}. */
    public void discardTargetHand(GameData gameData, UUID casterId, UUID targetPlayerId, com.github.laxika.magicalvibes.model.Card sourceCard) {
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, sourceCard, casterId,
                sourceCard.getName() + " - discard hand",
                List.of(new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER)),
                targetPlayerId, (UUID) null);
        discardHandEffectHandler.resolve(gameData, syntheticEntry,
                new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER));
    }
}
