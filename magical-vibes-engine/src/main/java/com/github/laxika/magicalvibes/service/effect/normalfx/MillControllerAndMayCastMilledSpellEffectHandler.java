package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastMilledSpellWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayCastMilledSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MillControllerAndMayCastMilledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayCastMilledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millAndCast = (MillControllerAndMayCastMilledSpellEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, millAndCast.count(),
                AmountContext.forStackEntry(entry, source)));
        int maxManaValue = Math.max(0, entry.getXValue());
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
        List<Card> castable = milled.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();

        for (int i = castable.size() - 1; i >= 0; i--) {
            Card card = castable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new MayCastMilledSpellWithoutPayingManaCostEffect(
                            GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                            new CardMaxManaValuePredicate(maxManaValue))),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
