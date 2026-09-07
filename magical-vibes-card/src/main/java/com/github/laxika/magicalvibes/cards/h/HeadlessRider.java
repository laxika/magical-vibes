package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "118")
public class HeadlessRider extends Card {

    private static final CreateTokenEffect ZOMBIE_TOKEN = CreateTokenEffect.blackZombie(1);

    public HeadlessRider() {
        // Whenever this creature or another nontoken Zombie you control dies, create a 2/2 black
        // Zombie creature token.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), ZOMBIE_TOKEN));
        addEffect(EffectSlot.ON_DEATH, ZOMBIE_TOKEN);
    }
}
