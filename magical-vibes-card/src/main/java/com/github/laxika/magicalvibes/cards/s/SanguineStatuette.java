package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "177")
public class SanguineStatuette extends Card {

    public SanguineStatuette() {
        // When this artifact enters, create a Blood token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));

        // Whenever you sacrifice a Blood token, you may have this artifact become a 3/3 Vampire
        // artifact creature with haste until end of turn.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                        new MayEffect(
                                new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.VAMPIRE),
                                        Set.of(Keyword.HASTE), null, Set.of(CardType.ARTIFACT)),
                                "have this artifact become a 3/3 Vampire artifact creature with haste?"
                        )
                ));
    }
}
