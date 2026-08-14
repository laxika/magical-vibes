package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "22")
public class AdvancedHoverguard extends Card {

    public AdvancedHoverguard() {
        addActivatedAbility(new ActivatedAbility(
                false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)),
                "{U}: This creature gains shroud until end of turn."));
    }
}
