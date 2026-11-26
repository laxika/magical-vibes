package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "204")
@CardRegistration(set = "DDH", collectorNumber = "31")
@CardRegistration(set = "SLD", collectorNumber = "1918")
@CardRegistration(set = "SLD", collectorNumber = "1923")
public class TitanicUltimatum extends Card {

    public TitanicUltimatum() {
        addSpellEffect(BoostEffect.creaturesYouControl(5, 5));
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.LIFELINK), Subjects.otherCreaturesYouControl()));
    }
}
