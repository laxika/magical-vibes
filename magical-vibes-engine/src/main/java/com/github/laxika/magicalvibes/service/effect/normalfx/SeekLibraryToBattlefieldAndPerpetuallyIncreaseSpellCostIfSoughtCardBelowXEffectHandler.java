package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Plunderer's Prize: the seek onto the battlefield is {@link SeekLibraryEffect}'s (the card passes through the
 * hand, so a "can't enter from libraries" rule doesn't stop it, and it enters with its enter abilities); then, if the
 * permanent it became is on the battlefield with mana value less than X, the spell perpetually costs more and returns
 * to its owner's hand.
 */
@Component
@RequiredArgsConstructor
public class SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffectHandler
        implements NormalEffectHandlerBean {

    private final SeekLibraryEffectHandler seekLibraryEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect) effect;
        int xValue = Math.max(0, entry.getXValue());
        seekLibraryEffectHandler.resolve(gameData, entry, new SeekLibraryEffect(new Fixed(1), seek.filter(),
                LibrarySearchDestination.BATTLEFIELD, new ManaValueBound(new XValue(), false, 0)));

        for (UUID permanentId : entry.getResolutionResults().outcome(StepKind.SEEK).objectIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && permanent.getCard().getManaValue() < xValue) {
                PerpetualCardCastCostSupport.rememberIncrease(
                        gameData, entry.getPhysicalCard(), seek.genericCastCostIncrease());
                entry.setReturnToHandAfterResolving(true);
                return;
            }
        }
    }
}
