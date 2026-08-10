package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "221")
public class NuisanceEngine extends Card {

    public NuisanceEngine() {
        // {2}, {T}: Create a 0/1 colorless Pest artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CreateTokenEffect("Pest", 0, 1, null,
                        List.of(CardSubtype.PEST), Set.of(), Set.of(CardType.ARTIFACT))),
                "{2}, {T}: Create a 0/1 colorless Pest artifact creature token."
        ));
    }
}
