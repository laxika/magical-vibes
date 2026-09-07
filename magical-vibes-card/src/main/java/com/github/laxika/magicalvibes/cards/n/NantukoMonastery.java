package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "142")
public class NantukoMonastery extends Card {

    public NantukoMonastery() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4,
                        List.of(CardSubtype.INSECT, CardSubtype.MONK),
                        Set.of(Keyword.FIRST_STRIKE),
                        Set.of(CardColor.GREEN, CardColor.WHITE))),
                "Threshold — {G}{W}: Nantuko Monastery becomes a 4/4 green and white Insect Monk creature with first strike until end of turn. It's still a land."
        ).withRequiredGraveyardCards(new CardTruePredicate(), 7, "cards in your graveyard"));
    }
}
