package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "57")
public class DeeptreadMerrow extends Card {

    public DeeptreadMerrow() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.SELF)), "{U}: Deeptread Merrow gains islandwalk until end of turn."));
    }
}
