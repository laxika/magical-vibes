package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "137")
public class Kaleidostone extends Card {

    public Kaleidostone() {
        // When this artifact enters, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        // {5}, {T}, Sacrifice this artifact: Add {W}{U}{B}{R}{G}.
        addActivatedAbility(new ActivatedAbility(true, "{5}",
                List.of(new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN)),
                "{5}, {T}, Sacrifice Kaleidostone: Add {W}{U}{B}{R}{G}."));
    }
}
