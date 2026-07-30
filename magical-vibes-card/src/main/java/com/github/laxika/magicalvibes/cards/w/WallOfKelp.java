package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "40")
public class WallOfKelp extends Card {

    public WallOfKelp() {
        // {U}{U}, {T}: Create a 0/1 blue Plant Wall creature token with defender named Kelp.
        addActivatedAbility(new ActivatedAbility(true, "{U}{U}",
                List.of(new CreateTokenEffect("Kelp", 0, 1, CardColor.BLUE,
                        List.of(CardSubtype.PLANT, CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.<CardType>of())),
                "{U}{U}, {T}: Create a 0/1 blue Plant Wall creature token with defender named Kelp."));
    }
}
