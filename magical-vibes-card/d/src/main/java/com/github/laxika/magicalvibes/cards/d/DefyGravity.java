package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "38")
@CardRegistration(set = "UMA", collectorNumber = "48")
public class DefyGravity extends Card {

    public DefyGravity() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()));
        addCastingOption(new FlashbackCast("{U}"));
    }
}
