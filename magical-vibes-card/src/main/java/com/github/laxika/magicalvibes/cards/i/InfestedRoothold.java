package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "76")
public class InfestedRoothold extends Card {

    public InfestedRoothold() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of()))),
                "Create a 1/1 green Insect creature token?"
        ));
    }
}
