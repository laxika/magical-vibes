package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "2")
public class BallynockTrapper extends Card {

    public BallynockTrapper() {
        // {T}: Tap target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Tap target creature.",
                TargetFilters.creature()
        ));

        // Whenever you cast a white spell, you may untap this creature.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap Ballynock Trapper?"));
    }
}
