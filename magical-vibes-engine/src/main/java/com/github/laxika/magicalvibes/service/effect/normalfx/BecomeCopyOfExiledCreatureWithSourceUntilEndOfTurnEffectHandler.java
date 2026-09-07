package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final BecomeCopyOfCardUntilEndOfTurnEffectHandler copyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID chosenCardId = entry.getTargetId();
        if (chosenCardId != null) {
            entry.setTargetId(null);
            ExiledCardEntry chosen = gameData.findExiledCard(chosenCardId);
            if (isEligible(chosen, sourcePermanentId)) {
                copyHandler.resolve(gameData, entry, new BecomeCopyOfCardUntilEndOfTurnEffect(chosen.card()));
            }
            return;
        }

        List<ExiledCardEntry> eligible = gameData.getExiledWithPermanentEntries(
                        sourcePermanentId, entry.getCard().getId()).stream()
                .filter(this::isEligible)
                .toList();
        if (eligible.isEmpty()) {
            return;
        }
        if (eligible.size() == 1) {
            copyHandler.resolve(gameData, entry,
                    new BecomeCopyOfCardUntilEndOfTurnEffect(eligible.getFirst().card()));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCreatureCopyChoice(
                entry.getControllerId(), sourcePermanentId,
                eligible.stream().map(exiled -> exiled.card().getId()).toList()));
    }

    private boolean isEligible(ExiledCardEntry entry) {
        return entry != null && !entry.faceDown() && entry.card().hasType(CardType.CREATURE);
    }

    private boolean isEligible(ExiledCardEntry entry, UUID sourcePermanentId) {
        return isEligible(entry) && sourcePermanentId.equals(entry.sourcePermanentId());
    }
}
