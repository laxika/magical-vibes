package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HA3", collectorNumber = "18")
public class TectonicReformation extends Card {

    public TectonicReformation() {
        addCycling("{2}");

        ActivatedAbility landCardCycling = new ActivatedAbility(false, "{R}",
                List.of(new DrawCardEffect(1)),
                "Cycling {R} ({R}, Discard this card: Draw a card.)");
        addEffect(EffectSlot.STATIC, new GrantHandActivatedAbilityToCardsEffect(
                landCardCycling, new CardTypePredicate(CardType.LAND)));
    }
}
