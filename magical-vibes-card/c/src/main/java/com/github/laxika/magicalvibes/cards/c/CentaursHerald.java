package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "118")
public class CentaursHerald extends Card {

    public CentaursHerald() {
        // {2}{G}, Sacrifice this creature: Create a 3/3 green Centaur creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SacrificeSelfCost(), new CreateTokenEffect(
                        "Centaur", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.CENTAUR), Set.<Keyword>of(), Set.<CardType>of())),
                "{2}{G}, Sacrifice Centaur's Herald: Create a 3/3 green Centaur creature token."
        ));
    }
}
