package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "79")
public class SkithiryxTheBlightDragon extends Card {

    public SkithiryxTheBlightDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{B}: Skithiryx, the Blight Dragon gains haste until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new RegenerateEffect()),
                "{B}{B}: Regenerate Skithiryx, the Blight Dragon."
        ));
    }
}
