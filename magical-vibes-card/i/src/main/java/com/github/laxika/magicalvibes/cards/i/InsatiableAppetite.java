package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ELD", collectorNumber = "162")
public class InsatiableAppetite extends Card {

    public InsatiableAppetite() {
        addSpellEffect(new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), BoostEffect.target(5, 5))),
                "Sacrifice a Food?",
                BoostEffect.target(3, 3)));
    }
}
