package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
        addEffect(EffectSlot.SPELL, new TargetPlayerCantCastSpellTypesThisTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));

        // ... and that player can't activate abilities that aren't mana abilities.
        addEffect(EffectSlot.SPELL, new TargetPlayerCantActivateNonManaAbilitiesThisTurnEffect());

        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
