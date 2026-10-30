package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random mana-value-matching card from a digital spellbook. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int manaValue = amountEvaluationService.evaluate(
                gameData, conjure.manaValue(), AmountContext.forStackEntry(entry, source));
        List<DraftFromSpellbookEffect.SpellbookCard> matching = new ArrayList<>();
        for (DraftFromSpellbookEffect.SpellbookCard reference : conjure.spellbook()) {
            Card card = cardCatalog.createCardIfImplemented(reference.setCode(), reference.collectorNumber());
            if (card != null && card.getManaValue() == manaValue) {
                matching.add(reference);
            }
        }
        if (matching.isEmpty()) {
            return;
        }

        DraftFromSpellbookEffect.SpellbookCard selected =
                matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        int index = entry.getResolvingEffectIndex();
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, List.of(
                new ConjureCardToBattlefieldEffect(selected.setCode(), selected.collectorNumber())));
    }
}
