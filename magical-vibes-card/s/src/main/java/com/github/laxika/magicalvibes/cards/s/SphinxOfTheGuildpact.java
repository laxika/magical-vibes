package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "RNA", collectorNumber = "241")
@CardRegistration(set = "2XM", collectorNumber = "290")
public class SphinxOfTheGuildpact extends Card {

    public SphinxOfTheGuildpact() {
        removeKeyword(Keyword.HEXPROOF);
        addStaticEffect(TargetingRestrictionEffect.hexproofFromMonocolored());
        addStaticEffect(new GrantColorEffect(CardColor.WHITE, Subjects.source()));
        addStaticEffect(new GrantColorEffect(CardColor.BLUE, Subjects.source()));
        addStaticEffect(new GrantColorEffect(CardColor.BLACK, Subjects.source()));
        addStaticEffect(new GrantColorEffect(CardColor.RED, Subjects.source()));
        addStaticEffect(new GrantColorEffect(CardColor.GREEN, Subjects.source()));
    }
}
