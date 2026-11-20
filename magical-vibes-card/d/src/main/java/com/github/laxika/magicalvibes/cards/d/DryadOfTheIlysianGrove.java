package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "169")
@CardRegistration(set = "THB", collectorNumber = "326")
@CardRegistration(set = "SLD", collectorNumber = "191")
@CardRegistration(set = "M3C", collectorNumber = "225")
public class DryadOfTheIlysianGrove extends Card {

    public DryadOfTheIlysianGrove() {
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));

        for (CardSubtype basicLandType : List.of(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST)) {
            addStaticEffect(new GrantSubtypeEffect(basicLandType, Subjects.landsYouControl()));
        }

        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                Subjects.permanentsYouControl(),
                new PermanentIsLandPredicate()
        ));
    }
}
