package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessReturnedLandHadNonbasicLandTypeEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Wonderscape Sage's discard rider using the land's last-known types at payment time. */
@Component
@RequiredArgsConstructor
public class DiscardUnlessReturnedLandHadNonbasicLandTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardUnlessReturnedLandHadNonbasicLandTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        Card returnedLand = sourceSnapshot == null ? null : sourceSnapshot.getChosenCard();
        if (returnedLand != null && returnedLand.getSubtypes().stream()
                .anyMatch(subtype -> CardSubtype.landTypes().contains(subtype)
                        && !CardSubtype.basicLandTypes().contains(subtype))) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1);
    }
}
