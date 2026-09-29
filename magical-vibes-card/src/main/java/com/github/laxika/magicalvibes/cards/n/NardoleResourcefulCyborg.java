package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.AllCountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "50")
@CardRegistration(set = "WHO", collectorNumber = "365")
public class NardoleResourcefulCyborg extends Card {

    public NardoleResourcefulCyborg() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE,
                        new AllCountersOnSource(),
                        new ManaRestriction.NoncreatureSpells())),
                "{T}: Add {U} for each counter on Nardole. Spend this mana only to cast noncreature spells."
        ));
    }
}
