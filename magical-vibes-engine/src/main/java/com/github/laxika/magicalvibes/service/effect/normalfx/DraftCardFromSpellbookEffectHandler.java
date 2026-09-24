package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftCardFromSpellbookEffect draft = (DraftCardFromSpellbookEffect) effect;
        List<Card> spellbook = draft.cardNames().stream()
                .map(this::createCardByName)
                .filter(card -> card != null)
                .toList();
        if (spellbook.isEmpty()) {
            return;
        }

        List<Card> choices = new ArrayList<>(spellbook);
        Collections.shuffle(choices);
        if (choices.size() > 3) {
            choices = new ArrayList<>(choices.subList(0, 3));
        }

        UUID draftPlayerId = draft.recipient() == DraftCardRecipient.TARGET_PLAYER
                ? entry.getTargetId() : entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(
                controllerName + " drafts a card from " + entry.getCard().getName() + "'s spellbook."));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftChoice(
                draftPlayerId, choices, entry.getCard().getName(), draft.revealChosenCard(),
                draft.exileChosenCard(), draft.putChosenCardOntoBattlefield(), draft.chosenCardEffects()));
    }

    private Card createCardByName(String name) {
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                Card card = printing.createCard();
                if (name.equals(card.getName())) {
                    return card;
                }
            }
        }
        return null;
    }
}
