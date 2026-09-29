package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "144")
public class TheMasterGallifreysEnd extends Card {

    public TheMasterGallifreysEnd() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        new MayEffect(new TheMasterGallifreysEndEffect(),
                                "Exile that creature and make an opponent face a villainous choice?")));
    }
}
