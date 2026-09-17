package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "19")
public class RatKingPalePiper extends Card {

    private static final CreateTokenEffect RAT_TOKEN = new CreateTokenEffect(
            1,
            "Rat",
            1,
            1,
            CardColor.BLACK,
            List.of(CardSubtype.RAT),
            Set.of(),
            Set.of());

    public RatKingPalePiper() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, RAT_TOKEN);
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()), RAT_TOKEN));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsTokenPredicate(), "Sacrifice a token"),
                        new DrawCardEffect()),
                "{2}, Sacrifice a token: Draw a card."));
    }
}
