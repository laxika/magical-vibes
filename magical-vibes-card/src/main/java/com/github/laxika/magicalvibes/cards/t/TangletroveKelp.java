package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "24")
@CardRegistration(set = "MKC", collectorNumber = "334")
public class TangletroveKelp extends Card {

    public TangletroveKelp() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new AnimatePermanentsEffect(
                new Fixed(6), new Fixed(6),
                List.of(CardSubtype.PLANT),
                Set.of(),
                null,
                Set.of(),
                GrantScope.OWN_PERMANENTS,
                EffectDuration.UNTIL_END_OF_TURN,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                ))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{2}, Sacrifice this creature: Draw a card."
        ));
    }
}
