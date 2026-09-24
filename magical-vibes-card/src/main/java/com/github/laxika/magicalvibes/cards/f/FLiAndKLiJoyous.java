package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "1")
public class FLiAndKLiJoyous extends Card {

    public FLiAndKLiJoyous() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forSpellSubtypes(
                        2,
                        Set.of(CardSubtype.DWARF, CardSubtype.EQUIPMENT, CardSubtype.SAGA),
                        List.of(ManaColor.RED))),
                "{T}: Add {R}{R}. Spend this mana only to cast a Dwarf, Equipment, or Saga spell."
        ));
    }
}
