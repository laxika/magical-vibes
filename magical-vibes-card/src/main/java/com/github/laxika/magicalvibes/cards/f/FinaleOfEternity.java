package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostXPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "91")
public class FinaleOfEternity extends Card {

    public FinaleOfEternity() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentToughnessAtMostXPredicate()
                )),
                "Targets must be creatures with toughness X or less"
        ), 0, 3).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(10),
                new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), Integer.MAX_VALUE)));
    }
}
