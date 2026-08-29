package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessCollectsEvidenceEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a ward ransom that requires collecting evidence. */
@Component
@RequiredArgsConstructor
public class CounterUnlessCollectsEvidenceEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessCollectsEvidenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessCollectsEvidenceEffect evidenceEffect =
                (CounterUnlessCollectsEvidenceEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        List<Card> graveyard = gameData.playerGraveyards.get(targetEntry.getControllerId());
        int totalManaValue = graveyard == null
                ? 0
                : graveyard.stream().mapToInt(Card::getManaValue).sum();
        if (totalManaValue < evidenceEffect.minimumManaValue()) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Collect evidence " + evidenceEffect.minimumManaValue()
                + " to prevent " + targetEntry.getCard().getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetEntry.getControllerId(), List.of(evidenceEffect),
                prompt, targetCardId));
    }
}
