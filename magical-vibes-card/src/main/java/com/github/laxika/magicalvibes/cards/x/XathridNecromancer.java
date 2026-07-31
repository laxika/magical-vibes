package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "123")
public class XathridNecromancer extends Card {

    public XathridNecromancer() {
        // Ally-death alone is "another" (the source is already gone); ON_DEATH covers this creature itself,
        // which is a Human and so always qualifies.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.HUMAN),
                tappedZombie()
        ));
        addEffect(EffectSlot.ON_DEATH, tappedZombie());
    }

    private static CreateTokenEffect tappedZombie() {
        return new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.<Keyword>of(), Set.<CardType>of(), true);
    }
}
