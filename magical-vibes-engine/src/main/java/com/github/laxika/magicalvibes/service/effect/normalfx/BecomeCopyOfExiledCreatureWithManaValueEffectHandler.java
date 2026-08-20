package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingValkiCopyChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BecomeCopyOfExiledCreatureWithManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfExiledCreatureWithManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            return;
        }

        List<Card> matching = gameData.exiledCards.stream()
                .filter(exiled -> sourcePermanentId.equals(exiled.sourcePermanentId()))
                .map(ExiledCardEntry::card)
                .filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE))
                .filter(card -> card.getManaValue() == entry.getXValue())
                .toList();
        if (matching.isEmpty()) {
            return;
        }
        if (matching.size() == 1) {
            becomeCopy(gameData, sourcePermanentId, matching.getFirst());
            return;
        }

        gameData.queueInteraction(new PendingValkiCopyChoice(sourcePermanentId, entry.getXValue()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                entry.getControllerId(), new ArrayList<>(matching), matching.stream().map(Card::getId).toList(),
                false, false, false, false, false, 0, null, 1,
                "Choose a creature card exiled with Valki, God of Lies to copy.", 1, false));
    }

    public void becomeCopy(GameData gameData, UUID sourcePermanentId, Card copiedCard) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }
        permanentCopierService.applyCloneCopy(source, copiedCard, null, null, Set.of());
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard() == entry.getCard()
                        || permanent.getCard().getId().equals(entry.getCard().getId())) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }
}
