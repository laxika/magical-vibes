package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LudevicCopySupport {

    private static final String OLAG_NAME = "Olag, Ludevic's Hubris";

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void resolveAfterTransform(GameData gameData, Permanent source) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (controllerId == null) {
            return;
        }

        List<Card> creatureCards = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry entry : gameData.exiledCards) {
                if (source.getId().equals(entry.sourcePermanentId())
                        && !entry.faceDown()
                        && entry.card().hasType(CardType.CREATURE)) {
                    creatureCards.add(entry.card());
                }
            }
        }

        if (creatureCards.isEmpty()) {
            return;
        }
        if (creatureCards.size() == 1) {
            applyCopy(source, creatureCards.getFirst(), creatureCards.size());
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.LudevicCopyChoice(controllerId, source.getId(), creatureCards));
    }

    public void applyCopy(Permanent source, Card target, int creatureCardCount) {
        List<CardColor> targetColors = target.getColors() == null
                ? new ArrayList<>()
                : new ArrayList<>(target.getColors());
        if (targetColors.isEmpty() && target.getColor() != null) {
            targetColors.add(target.getColor());
        }
        if (!targetColors.contains(CardColor.BLUE)) {
            targetColors.add(CardColor.BLUE);
        }
        if (!targetColors.contains(CardColor.BLACK)) {
            targetColors.add(CardColor.BLACK);
        }

        permanentCopierService.applyCloneCopy(source, target, 4, 4, java.util.Set.of());
        Card copy = source.getCard();
        copy.setName(OLAG_NAME);
        copy.setColor(targetColors.getFirst());
        copy.setColors(List.copyOf(targetColors));

        EnumSet<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        supertypes.addAll(copy.getSupertypes());
        supertypes.add(CardSupertype.LEGENDARY);
        copy.setSupertypes(supertypes);

        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        if (!subtypes.contains(CardSubtype.ZOMBIE)) {
            subtypes.add(CardSubtype.ZOMBIE);
        }
        copy.setSubtypes(List.copyOf(subtypes));

        source.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE,
                source.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)
                        + creatureCardCount);
    }
}
