package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureOfEachManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves the dynamic mana-value range by expanding it into ordinary conjure effects. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCreatureOfEachManaValueToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final CardCatalog cardCatalog;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCreatureOfEachManaValueToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureRandomCreatureOfEachManaValueToBattlefieldEffect) effect;
        int maxManaValue = amountEvaluationService.evaluate(
                gameData, conjure.maxManaValue(), AmountContext.forStackEntry(entry, null));
        if (maxManaValue <= 0) {
            return;
        }

        List<CardEffect> conjures = new ArrayList<>();
        for (int manaValue = 1; manaValue <= maxManaValue; manaValue++) {
            findRandomCreatureName(manaValue).ifPresent(name ->
                    conjures.add(new ConjureCardToBattlefieldEffect(name)));
        }

        int index = entry.getEffectsToResolve().indexOf(effect);
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, conjures);
    }

    private Optional<String> findRandomCreatureName(int manaValue) {
        List<String> matchingNames = new ArrayList<>();
        Set<String> inspectedClasses = new HashSet<>();
        for (CardSet cardSet : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(cardSet)) {
                if (!inspectedClasses.add(printing.cardClassName())) {
                    continue;
                }
                Card card = printing.createCard();
                if (card.hasType(CardType.CREATURE) && card.getManaValue() == manaValue) {
                    matchingNames.add(card.getName());
                }
            }
        }
        if (matchingNames.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(matchingNames.get(ThreadLocalRandom.current().nextInt(matchingNames.size())));
    }
}
