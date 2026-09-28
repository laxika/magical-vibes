package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "715")
public class BlackTomCassidy extends Card {

    public BlackTomCassidy() {
        // {T}: Add {G}. If you control another Mutant, you gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.GREEN),
                        new ConditionalEffect(
                                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.MUTANT)),
                                new GainLifeEffect(1))),
                "{T}: Add {G}. If you control another Mutant, you gain 1 life."
        ));
    }
}
