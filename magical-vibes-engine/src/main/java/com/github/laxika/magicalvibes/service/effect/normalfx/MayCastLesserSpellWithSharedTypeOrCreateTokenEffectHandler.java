package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLesserSpellWithSharedTypeOrCreateTokenEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("mayCastLesserSpellWithSharedTypeOrCreateTokenNormalEffectHandler")
@RequiredArgsConstructor
public class MayCastLesserSpellWithSharedTypeOrCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

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
        List<Card> eligibleCards = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() < effectiveTriggeringManaValue)
                .filter(card -> sharesCardType(card, triggeringSpell))
                .toList();

        if (eligibleCards.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, castEffect.tokenEffect());
            return;
        }

        for (int i = eligibleCards.size() - 1; i >= 0; i--) {
            Card card = eligibleCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(castEffect),
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
                        && stackEntry.getCard().getId().equals(triggeringCardId)
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
                .filter(stackEntry -> stackEntry.getCard() != null && stackEntry.getCard().getId().equals(cardId))
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
