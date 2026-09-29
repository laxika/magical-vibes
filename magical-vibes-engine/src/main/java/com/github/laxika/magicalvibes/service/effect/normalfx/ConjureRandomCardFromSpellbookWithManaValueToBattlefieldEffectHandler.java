package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random mana-value-matching card from a digital spellbook. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect) effect;
        int manaValue = amountEvaluationService.evaluate(
                gameData, conjure.manaValue(), AmountContext.forStackEntry(entry, null));
        List<String> matchingNames = new ArrayList<>();
        for (String cardName : conjure.cardNames()) {
            Card card = findCard(cardName);
            if (card != null && card.getManaValue() == manaValue) {
                matchingNames.add(card.getName());
            }
        }
        if (matchingNames.isEmpty()) {
            return;
        }

        String selectedName = matchingNames.get(ThreadLocalRandom.current().nextInt(matchingNames.size()));
        int index = entry.getResolvingEffectIndex();
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, List.of(new ConjureCardToBattlefieldEffect(selectedName)));
    }

    private Card findCard(String cardName) {
        Set<String> inspectedClasses = new HashSet<>();
        for (CardSet cardSet : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(cardSet)) {
                if (!inspectedClasses.add(printing.cardClassName())) {
                    continue;
                }
                Card card = printing.createCard();
                if (cardName.equals(card.getName())) {
                    return card;
                }
            }
        }
        return null;
    }
}
