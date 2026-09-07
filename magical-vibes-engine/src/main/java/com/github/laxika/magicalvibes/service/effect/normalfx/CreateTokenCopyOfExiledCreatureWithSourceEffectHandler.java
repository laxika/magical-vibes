package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfExiledCreatureWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfExiledCreatureWithSourceEffect.class;
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
                createTokenCopy(gameData, entry, effect, chosen.card());
            }
            return;
        }

        List<ExiledCardEntry> eligible = eligibleCards(gameData, entry, sourcePermanentId);
        if (eligible.isEmpty()) {
            return;
        }
        if (eligible.size() == 1) {
            createTokenCopy(gameData, entry, effect, eligible.getFirst().card());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        String sourceName = entry.getCard() == null ? "the source permanent" : entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCreatureCopyChoice(
                entry.getControllerId(), sourcePermanentId,
                eligible.stream().map(exiled -> exiled.card().getId()).toList(),
                sourceName));
    }

    private List<ExiledCardEntry> eligibleCards(GameData gameData, StackEntry entry,
                                                  UUID sourcePermanentId) {
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        return gameData.getExiledWithPermanentEntries(sourcePermanentId, sourceCardId).stream()
                .filter(exiled -> isEligible(exiled, sourcePermanentId))
                .toList();
    }

    private boolean isEligible(ExiledCardEntry exiled, UUID sourcePermanentId) {
        return exiled != null
                && sourcePermanentId.equals(exiled.sourcePermanentId())
                && !exiled.faceDown()
                && exiled.card().hasType(CardType.CREATURE);
    }

    private void createTokenCopy(GameData gameData, StackEntry entry, CardEffect effect, Card card) {
        CreateTokenCopyOfExiledCreatureWithSourceEffect copyEffect =
                (CreateTokenCopyOfExiledCreatureWithSourceEffect) effect;
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(card), sourcePermanent,
                copyEffect.tokenCopyEffect());
    }
}
