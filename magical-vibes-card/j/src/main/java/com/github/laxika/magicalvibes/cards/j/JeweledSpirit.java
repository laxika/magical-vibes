package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "12")
public class JeweledSpirit extends Card {

    public JeweledSpirit() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate()),
                        new GrantProtectionChoiceUntilEndOfTurnEffect(true, false, GrantScope.SELF, null)
                ),
                "Sacrifice two lands: This creature gains protection from artifacts or from the color of your choice until end of turn."
        ));
    }
}
