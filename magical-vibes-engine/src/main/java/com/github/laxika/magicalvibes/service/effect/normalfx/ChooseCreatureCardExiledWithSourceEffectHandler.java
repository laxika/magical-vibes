package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChooseCreatureCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public ChooseCreatureCardExiledWithSourceEffectHandler(
            GameQueryService gameQueryService,
            InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) return;

        List<ExiledCardEntry> eligible = gameData.getExiledWithPermanentEntries(
                        source.getId(), source.getCard().getId()).stream()
                .filter(exiled -> exiled.sourcePermanentId() != null
                        && exiled.sourcePermanentId().equals(source.getId()))
                .filter(exiled -> !exiled.faceDown() && exiled.card().hasType(CardType.CREATURE))
                .toList();
        if (eligible.isEmpty()) return;
        if (eligible.size() == 1) {
            source.setLastChosenExiledCard(eligible.getFirst().card());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.KohExiledCreatureChoice(
                entry.getControllerId(), source.getId(),
                eligible.stream().map(exiled -> exiled.card().getId()).toList()));
    }
}
