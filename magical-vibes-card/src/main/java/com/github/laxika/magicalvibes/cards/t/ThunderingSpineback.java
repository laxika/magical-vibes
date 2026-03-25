package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "210")
public class ThunderingSpineback extends Card {

    public ThunderingSpineback() {
        // Other Dinosaurs you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));

        // {5}{G}: Create a 3/3 green Dinosaur creature token with trample.
        addActivatedAbility(new ActivatedAbility(
                false, "{5}{G}",
                List.of(new CreateTokenEffect("Dinosaur", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.DINOSAUR), Set.of(Keyword.TRAMPLE), Set.of())),
                "{5}{G}: Create a 3/3 green Dinosaur creature token with trample."
        ));
    }
}
