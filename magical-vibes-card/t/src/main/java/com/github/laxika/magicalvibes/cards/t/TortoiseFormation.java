package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ALA", collectorNumber = "61")
public class TortoiseFormation extends Card {

    public TortoiseFormation() {
        addSpellEffect(new GrantKeywordEffect(Keyword.SHROUD, Subjects.otherCreaturesYouControl()));
    }
}
