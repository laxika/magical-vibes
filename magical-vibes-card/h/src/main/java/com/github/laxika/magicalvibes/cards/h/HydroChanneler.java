package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "54")
public class HydroChanneler extends Card {

    public HydroChanneler() {
        // {T}: Add {U}. Spend this mana only to cast an instant or sorcery spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE,
                        1,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))
                )),
                "{T}: Add {U}. Spend this mana only to cast an instant or sorcery spell."
        ));

        // {1}, {T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.INSTANT_SORCERY_ONLY)),
                "{1}, {T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."
        ));
    }
}
