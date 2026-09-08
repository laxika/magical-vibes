package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "248")
public class KarnsBastion extends Card {

    public KarnsBastion() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ProliferateEffect()),
                "{4}, {T}: Proliferate."
        ));
    }
}
