package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class DraftFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftFromSpellbookEffect draft = (DraftFromSpellbookEffect) effect;
        if (!draft.cardFactories().isEmpty()) {
            List<Supplier<? extends Card>> shuffled = new ArrayList<>(draft.cardFactories());
            Collections.shuffle(shuffled);
            List<ChooseOneEffect.ChooseOneOption> options = shuffled.stream()
                    .limit(draft.offeredCardCount())
                    .map(factory -> {
                        Card preview = factory.get();
                        return new ChooseOneEffect.ChooseOneOption(
                                preview.getName(),
                                new ConjureCardOntoBattlefieldEffect(factory, Set.of(CardType.LAND)));
                    })
                    .toList();
            playerInputService.beginChooseModeChoice(
                    gameData, entry.getControllerId(), entry.getCard(), new ChooseOneEffect(options),
                    false, entry.getSourcePermanentId());
            return;
        }

        List<Card> availableCards = draft.spellbook().stream()
                .map(this::createCardIfImplemented)
                .filter(card -> card != null)
                .toList();
        if (availableCards.isEmpty()) {
            return;
        }

        List<Card> offeredCards = new ArrayList<>(availableCards);
        Collections.shuffle(offeredCards);
        offeredCards = new ArrayList<>(offeredCards.subList(
                0, Math.min(draft.offeredCardCount(), offeredCards.size())));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookCardChoice(
                entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId(),
                offeredCards,
                "Choose a card from " + entry.getCard().getName() + "'s spellbook.",
                draft.mode()));
    }

    private Card createCardIfImplemented(DraftFromSpellbookEffect.SpellbookCard reference) {
        CardSet set = CardSet.findByCode(reference.setCode());
        if (set == null) {
            return null;
        }
        try {
            return cardCatalog.findByCollectorNumber(set, reference.collectorNumber()).createCard();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
