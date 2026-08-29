package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "86")
public class Cryptbreaker extends Card {

    public Cryptbreaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new DiscardCardTypeCost(null, null), CreateTokenEffect.blackZombie(1)),
                "{1}{B}, {T}, Discard a card: Create a 2/2 black Zombie creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1)
                ),
                "Tap three untapped Zombies you control: You draw a card and you lose 1 life."
        ));
    }
}
