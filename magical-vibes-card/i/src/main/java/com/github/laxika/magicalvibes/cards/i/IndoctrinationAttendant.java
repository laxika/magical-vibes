package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "16")
public class IndoctrinationAttendant extends Card {

    public IndoctrinationAttendant() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandThenEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        miteToken(),
                        "another permanent"),
                "Return another permanent you control to its owner's hand?"));
    }

    private static CreateTokenEffect miteToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Mite",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                Set.of(Keyword.TOXIC),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(
                        EffectSlot.STATIC, SequenceEffect.of(new CantBlockEffect(), new ToxicEffect(1))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
