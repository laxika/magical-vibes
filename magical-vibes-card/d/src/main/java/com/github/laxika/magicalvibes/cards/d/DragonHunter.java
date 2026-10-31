package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CanBlockCreaturesWithSubtypeAsThoughReachEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "10")
public class DragonHunter extends Card {

    public DragonHunter() {
        addStaticEffect(new ProtectionFromSubtypesEffect(Set.of(CardSubtype.DRAGON)));
        addStaticEffect(new CanBlockCreaturesWithSubtypeAsThoughReachEffect(CardSubtype.DRAGON));
    }
}
