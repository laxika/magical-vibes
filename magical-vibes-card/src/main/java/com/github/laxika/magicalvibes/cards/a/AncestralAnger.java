package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOS", collectorNumber = "106")
@CardRegistration(set = "INR", collectorNumber = "141")
public class AncestralAnger extends Card {

    public AncestralAnger() {
        // Target creature gains trample and gets +X/+0 until end of turn, where X is 1 plus
        // the number of cards named Ancestral Anger in your graveyard.
        // Draw a card.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                // X is 1 plus the number of cards named Ancestral Anger in your graveyard.
                new Sum(new Fixed(1), new CardsInGraveyard(new CardNamedPredicate("Ancestral Anger"), CountScope.CONTROLLER)),
                new Fixed(0)
        )).addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
          .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
