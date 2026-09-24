package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithSameName;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "2XM", collectorNumber = "251")
public class EndlessAtlas extends Card {

    public EndlessAtlas() {
        // {2}, {T}: Draw a card. Activate only if you control three or more lands with the same name.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1)),
                "{2}, {T}: Draw a card. Activate only if you control three or more lands with the same name."
        ).withActivationCondition(
                new ControlsPermanentsWithSameName(3, new PermanentIsLandPredicate()),
                "Activate only if you control three or more lands with the same name."
        ));
    }
}
