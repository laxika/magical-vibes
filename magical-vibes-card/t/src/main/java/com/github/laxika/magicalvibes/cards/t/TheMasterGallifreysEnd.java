package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "144")
@CardRegistration(set = "WHO", collectorNumber = "427")
@CardRegistration(set = "WHO", collectorNumber = "543")
@CardRegistration(set = "WHO", collectorNumber = "749")
@CardRegistration(set = "WHO", collectorNumber = "1018")
@CardRegistration(set = "WHO", collectorNumber = "1134")
public class TheMasterGallifreysEnd extends Card {

    public TheMasterGallifreysEnd() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        new MayEffect(new TheMasterGallifreysEndEffect(),
                                "Exile that creature and make an opponent face a villainous choice?")));
    }
}
