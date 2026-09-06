package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "300")
public class Wellwisher extends Card {

    public Wellwisher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(new GainLifeEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.ANY_PLAYER))),
                "{T}: You gain 1 life for each Elf on the battlefield."
        ));
    }
}
