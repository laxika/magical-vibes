package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProwlToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "REX", collectorNumber = "4")
@CardRegistration(set = "REX", collectorNumber = "30")
public class HuntingVelociraptor extends Card {

    public HuntingVelociraptor() {
        // Dinosaur spells you cast have prowl {2}{R}.
        addEffect(EffectSlot.STATIC, new GrantProwlToSpellsEffect(
                "{2}{R}", new CardSubtypePredicate(CardSubtype.DINOSAUR)));
    }
}
