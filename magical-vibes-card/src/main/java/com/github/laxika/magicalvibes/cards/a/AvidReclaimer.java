package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "201")
public class AvidReclaimer extends Card {

    public AvidReclaimer() {
        // "{T}: Add {G} or {U}. If you control a Nissa planeswalker, you gain 2 life."
        // "Add {G} or {U}" is two separate mana abilities (Adarkar Wastes / Land Cap idiom). The
        // conditional life gain is a mana-ability rider evaluated inline when the ability resolves.
        ConditionalEffect nissaLife = new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.NISSA)),
                new GainLifeEffect(2));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), nissaLife),
                "{T}: Add {G}. If you control a Nissa planeswalker, you gain 2 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), nissaLife),
                "{T}: Add {U}. If you control a Nissa planeswalker, you gain 2 life."
        ));
    }
}
