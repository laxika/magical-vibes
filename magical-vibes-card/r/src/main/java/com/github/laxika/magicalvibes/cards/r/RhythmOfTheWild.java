package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "201")
@CardRegistration(set = "SLD", collectorNumber = "1740")
@CardRegistration(set = "RVR", collectorNumber = "217")
@CardRegistration(set = "PZA", collectorNumber = "12")
@CardRegistration(set = "LCC", collectorNumber = "287")
@CardRegistration(set = "NEC", collectorNumber = "142")
public class RhythmOfTheWild extends Card {

    public RhythmOfTheWild() {
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(Set.of(CardType.CREATURE)));
        addStaticEffect(new GrantKeywordEffect(Keyword.RIOT, Subjects.creaturesYouControl(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
