package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEmergeSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "158")
public class FoulEmissary extends Card {

    public FoulEmissary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                        4, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.ON_DEATH,
                new CreateTokenForEmergeSacrificeEffect(new CreateTokenEffect(
                        1, "Eldrazi Horror", 3, 2, null,
                        List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false)));
    }
}
