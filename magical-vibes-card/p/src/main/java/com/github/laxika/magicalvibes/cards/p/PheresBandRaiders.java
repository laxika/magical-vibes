package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "133")
public class PheresBandRaiders extends Card {

    public PheresBandRaiders() {
        addTrigger(Triggers.becomesUntapped(),
                new MayPayManaEffect("{2}{G}", new CreateTokenEffect(
                        "Centaur", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.CENTAUR), Set.of(), Set.of(CardType.ENCHANTMENT)),
                        "Pay {2}{G} to create a 3/3 green Centaur enchantment creature token?"));
    }
}
