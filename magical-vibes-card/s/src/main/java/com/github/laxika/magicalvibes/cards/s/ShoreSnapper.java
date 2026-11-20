package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ALA", collectorNumber = "87")
public class ShoreSnapper extends Card {

    public ShoreSnapper() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new GrantKeywordEffect(Keyword.ISLANDWALK, Subjects.source())), "{U}: This creature gains islandwalk until end of turn."));
    }
}
