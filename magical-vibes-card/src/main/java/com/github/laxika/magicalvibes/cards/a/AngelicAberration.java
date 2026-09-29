package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBaseToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "39")
public class AngelicAberration extends Card {

    private static final PermanentAllOfPredicate SMALL_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentBasePowerAtMostPredicate(1),
                    new PermanentBaseToughnessAtMostPredicate(1)
            ))
    ));

    public AngelicAberration() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeAnyNumberOfPermanentsEffect(SMALL_CREATURE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new EventValue(),
                "Eldrazi Angel",
                4,
                4,
                null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.ANGEL),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE),
                Set.of()));
    }
}
