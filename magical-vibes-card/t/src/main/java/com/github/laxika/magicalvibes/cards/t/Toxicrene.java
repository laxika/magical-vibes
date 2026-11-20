package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "40K", collectorNumber = "101")
public class Toxicrene extends Card {

    public Toxicrene() {
        addStaticEffect(new LosesAllAbilitiesEffect(Subjects.allLands()));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), Subjects.allLands()));
    }
}
