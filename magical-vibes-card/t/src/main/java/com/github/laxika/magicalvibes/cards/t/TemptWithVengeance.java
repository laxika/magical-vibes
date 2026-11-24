package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "C13", collectorNumber = "125")
@CardRegistration(set = "CMM", collectorNumber = "263")
@CardRegistration(set = "CMM", collectorNumber = "551")
@CardRegistration(set = "TDC", collectorNumber = "239")
public class TemptWithVengeance extends Card {

    public TemptWithVengeance() {
        CreateTokenEffect elementals = new CreateTokenEffect(new XValue(), "Elemental", 1, 1, CardColor.RED,
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), Set.of());
        addSpellEffect(elementals);
        addSpellEffect(TemptingOfferEffect.of(
                "Create X 1/1 red Elemental creature tokens with haste? If you do, its controller creates X more.",
                elementals::forController));
    }
}
