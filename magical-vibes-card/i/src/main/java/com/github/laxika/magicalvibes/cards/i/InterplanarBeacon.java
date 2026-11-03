package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "247")
public class InterplanarBeacon extends Card {

    public InterplanarBeacon() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.PLANESWALKER)),
                new GainLifeEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(AwardAnyColorManaEffect.ofDifferentColors(2, ManaSpendRestriction.PLANESWALKER_SPELLS)),
                "{1}, {T}: Add two mana of different colors. Spend this mana only to cast planeswalker spells."
        ));
    }
}
