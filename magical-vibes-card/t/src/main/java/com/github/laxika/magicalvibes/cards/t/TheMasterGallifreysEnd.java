package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "144")
@CardRegistration(set = "WHO", collectorNumber = "427")
@CardRegistration(set = "WHO", collectorNumber = "543")
@CardRegistration(set = "WHO", collectorNumber = "749")
@CardRegistration(set = "WHO", collectorNumber = "1018")
@CardRegistration(set = "WHO", collectorNumber = "1134")
public class TheMasterGallifreysEnd extends Card {

    public TheMasterGallifreysEnd() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        new MayEffect(new TheMasterGallifreysEndEffect(),
                                "Exile that creature and make an opponent face a villainous choice?")));
        // "A nontoken artifact creature you control" includes The Master itself while it is one, and the
        // ally slot only watches other creatures.
        addEffect(EffectSlot.ON_DEATH,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        new MayEffect(new TheMasterGallifreysEndEffect(),
                                "Exile that creature and make an opponent face a villainous choice?")));
    }
}
