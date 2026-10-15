package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "164")
public class WarmWelcome extends Card {

    public WarmWelcome() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Citizen", 1, 1,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));
    }
}
