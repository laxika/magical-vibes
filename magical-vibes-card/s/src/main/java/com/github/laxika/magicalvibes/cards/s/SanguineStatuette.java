package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "177")
@CardRegistration(set = "DBL", collectorNumber = "444")
public class SanguineStatuette extends Card {

    public SanguineStatuette() {
        // When this artifact enters, create a Blood token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));

        // Whenever you sacrifice a Blood token, you may have this artifact become a 3/3 Vampire
        // artifact creature with haste until end of turn.
        addTrigger(Triggers.sacrificed(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD))),
                        ObjectRelation.YOU_CONTROL),
                new MayEffect(
                                new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.VAMPIRE),
                                        Set.of(Keyword.HASTE), null, Set.of(CardType.ARTIFACT)),
                                "have this artifact become a 3/3 Vampire artifact creature with haste?"
                        ));
    }
}
