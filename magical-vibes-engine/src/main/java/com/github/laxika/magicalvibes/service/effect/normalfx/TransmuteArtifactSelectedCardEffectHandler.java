package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactCardDispositionEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactSelectedCardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransmuteArtifactSelectedCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransmuteArtifactSelectedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card selectedCard = entry.getChosenObjectCard();
        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (selectedCard == null || sacrificed == null) {
            return;
        }

        int difference = selectedCard.getManaValue() - sacrificed.getCard().getManaValue();
        if (difference <= 0 || gameData.pendingEffectResolutionEntry == null) {
            return;
        }

        TransmuteArtifactCardDispositionEffect toBattlefield =
                new TransmuteArtifactCardDispositionEffect(selectedCard.getId(), true);
        TransmuteArtifactCardDispositionEffect toGraveyard =
                new TransmuteArtifactCardDispositionEffect(selectedCard.getId(), false);
        gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                gameData.pendingEffectResolutionIndex + 1,
                java.util.List.of(new MayPayManaEffect(
                        "{" + difference + "}",
                        toBattlefield,
                        "Pay {" + difference + "} to put " + selectedCard.getName()
                                + " onto the battlefield?",
                        toGraveyard)));
    }
}
