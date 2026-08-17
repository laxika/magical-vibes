package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastAnyNumberOfEldraziSpellsFromOutsideGameEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromOutsideGameWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Spawnsire of Ulamog's outside-the-game Eldrazi cast ability. */
@Component
@RequiredArgsConstructor
public class CastAnyNumberOfEldraziSpellsFromOutsideGameEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastAnyNumberOfEldraziSpellsFromOutsideGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> sideboard = gameData.playerSideboards.get(controllerId);
        if (sideboard == null || sideboard.isEmpty()) {
            return;
        }

        List<Card> eldraziSpells = sideboard.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardSubtypePredicate(CardSubtype.ELDRAZI),
                        entry.getCard() == null ? null : entry.getCard().getId()))
                .toList();
        for (int i = eldraziSpells.size() - 1; i >= 0; i--) {
            Card card = eldraziSpells.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastFromOutsideGameWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }
}
