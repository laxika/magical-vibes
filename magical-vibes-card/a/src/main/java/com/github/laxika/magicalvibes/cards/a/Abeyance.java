package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantActivateNonManaAbilitiesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastSpellTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "1")
public class Abeyance extends Card {

    public Abeyance() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));

        // Until end of turn, target player can't cast instant or sorcery spells, ...
        addSpellEffect(new TargetPlayerCantCastSpellTypesThisTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));

        // ... and that player can't activate abilities that aren't mana abilities.
        addSpellEffect(new TargetPlayerCantActivateNonManaAbilitiesThisTurnEffect());

        // Draw a card.
        addSpellEffect(new DrawCardEffect(1));
    }
}
