package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLesserSpellWithSharedTypeOrCreateTokenEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Component("mayCastLesserSpellWithSharedTypeOrCreateTokenNormalEffectHandler")
@RequiredArgsConstructor
public class MayCastLesserSpellWithSharedTypeOrCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SplitCardCastSupport splitCardCastSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastLesserSpellWithSharedTypeOrCreateTokenEffect castEffect =
                (MayCastLesserSpellWithSharedTypeOrCreateTokenEffect) effect;
        Card triggeringSpell = findTriggeringSpell(gameData, entry.getTriggeringCardId(), entry.getControllerId());
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());

        if (triggeringSpell == null || hand == null) {
            createTokenEffectHandler.resolve(gameData, entry, castEffect.tokenEffect());
            return;
        }

        int triggeringManaValue = entry.getEventValue();
        if (triggeringManaValue == 0) {
            triggeringManaValue = triggeringSpell.getManaValue();
            StackEntry spellEntry = findStackEntry(gameData, triggeringSpell.getId());
            if (spellEntry != null) {
                triggeringManaValue += spellEntry.getXValue();
            }
        }
        final int effectiveTriggeringManaValue = triggeringManaValue;
        // "A spell with lesser mana value that shares a card type" considers only the half of a split
        // card being cast (CR 709.3a), so a split card is offered as the halves that qualify.
        Predicate<Card> qualifies = spell -> spell.getManaValue() < effectiveTriggeringManaValue
                && sharesCardType(spell, triggeringSpell);
        Map<Card, List<Integer>> eligible = new LinkedHashMap<>();
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                continue;
            }
            if (SplitCardCastSupport.isSplitCard(card)) {
                List<Integer> modes = splitCardCastSupport.modesMatching(
                        gameData, card, entry.getControllerId(), qualifies);
                if (!modes.isEmpty()) {
                    eligible.put(card, modes);
                }
            } else if (qualifies.test(card)) {
                eligible.put(card, null);
            }
        }

        if (eligible.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, castEffect.tokenEffect());
            return;
        }

        List<Card> eligibleCards = new ArrayList<>(eligible.keySet());
        for (int i = eligibleCards.size() - 1; i >= 0; i--) {
            Card card = eligibleCards.get(i);
            List<Integer> splitModes = eligible.get(card);
            List<CardEffect> effects = splitModes == null ? List.of(castEffect) : List.of(castEffect,
                    new MayCastFromHandWithoutPayingManaCostEffect(true).withCastableSplitModes(splitModes));
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    effects,
                    "Cast " + card.getName() + " without paying its mana cost?",
                    null,
                    null,
                    entry.getSourcePermanentId()));
        }
    }

    private Card findTriggeringSpell(GameData gameData, UUID triggeringCardId, UUID controllerId) {
        if (triggeringCardId == null) {
            return null;
        }
        Card cardOnStack = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getCard() != null
                        && stackEntry.getTargetableId().equals(triggeringCardId)
                        && isSpell(stackEntry.getEntryType()))
                .map(StackEntry::getCard)
                .findFirst()
                .orElse(null);
        if (cardOnStack != null) {
            return cardOnStack;
        }
        return gameData.getSpellsCastThisTurn(controllerId).stream()
                .filter(card -> card.getId().equals(triggeringCardId))
                .findFirst()
                .orElse(null);
    }

    private StackEntry findStackEntry(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getCard() != null && stackEntry.getTargetableId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private boolean sharesCardType(Card first, Card second) {
        for (CardType type : CardType.values()) {
            if (first.hasType(type) && second.hasType(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpell(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
