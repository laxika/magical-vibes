package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ApplejackFamilyGatheringEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Applejack's choice of an owned outside-the-game toy. */
@Component
public class ApplejackFamilyGatheringEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public ApplejackFamilyGatheringEffectHandler(InteractionHandlerRegistry interactionHandlerRegistry) {
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ApplejackFamilyGatheringEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> toys = gameData.playerSideboards.getOrDefault(controllerId, List.of());
        if (!toys.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ApplejackToyChoice(controllerId, toys));
        }
    }

    /** Adds the chosen toy's token and Family Gathering follow-up effects to the parked entry. */
    public void completeChoice(GameData gameData, Card toy) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending Applejack effect resolution");
        }

        boolean wings = hasWings(toy);
        boolean horn = hasHorn(toy);
        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(toyToken(toy, wings));
        if (horn) {
            followUps.add(new ScryEffect(2));
        } else if (!wings) {
            followUps.add(foodToken());
        }
        entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, followUps);
    }

    private static CreateTokenEffect toyToken(Card toy, boolean wings) {
        Set<CardColor> colors = toy.getColors() == null ? Set.of() : Set.copyOf(toy.getColors());
        List<CardSubtype> subtypes = toy.getSubtypes() == null ? List.of() : List.copyOf(toy.getSubtypes());
        Set<Keyword> keywords = wings ? Set.of(Keyword.FLYING) : Set.of();
        return new CreateTokenEffect(1, toy.getName(), 2, 2, toy.getColor(), colors,
                subtypes, keywords, Set.of());
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }

    private static boolean hasWings(Card card) {
        return card.getSubtypes() != null && card.getSubtypes().contains(CardSubtype.PEGASUS)
                || card.getKeywords() != null && card.getKeywords().contains(Keyword.FLYING);
    }

    private static boolean hasHorn(Card card) {
        return card.getSubtypes() != null && card.getSubtypes().contains(CardSubtype.UNICORN);
    }
}
