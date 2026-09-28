package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "137")
@CardRegistration(set = "PIP", collectorNumber = "358")
@CardRegistration(set = "PIP", collectorNumber = "665")
@CardRegistration(set = "PIP", collectorNumber = "886")
public class NukaColaVendingMachine extends Card {

    public NukaColaVendingMachine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(CreateTokenEffect.ofFoodToken(1)),
                "{1}, {T}: Create a Food token."
        ));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                        CreateTokenEffect.ofTappedTreasureToken(1)));
    }
}
