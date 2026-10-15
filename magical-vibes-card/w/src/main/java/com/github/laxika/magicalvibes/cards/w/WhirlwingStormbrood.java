package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DynamicSoar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Whirlwing Stormbrood // Dynamic Soar (TDM 234). */
@CardRegistration(set = "TDM", collectorNumber = "234")
public class WhirlwingStormbrood extends Card {

    public WhirlwingStormbrood() {
        setBackFaceCard(new DynamicSoar());
        addCastingOption(new OmenCast());
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.SORCERY),
                new CardSubtypePredicate(CardSubtype.DRAGON)
        ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "DynamicSoar";
    }
}
