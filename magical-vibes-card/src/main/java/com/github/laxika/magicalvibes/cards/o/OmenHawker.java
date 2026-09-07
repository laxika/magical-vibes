package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "70")
public class OmenHawker extends Card {

    public OmenHawker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.COLORLESS, 1, new ManaRestriction.Abilities()),
                        new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.Abilities())
                ),
                "{T}: Add {C}{U}. Spend this mana only to activate abilities."
        ));
    }
}
