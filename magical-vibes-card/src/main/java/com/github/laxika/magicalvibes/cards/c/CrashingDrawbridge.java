package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "217")
public class CrashingDrawbridge extends Card {

    public CrashingDrawbridge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)),
                "{T}: Creatures you control gain haste until end of turn."
        ));
    }
}
