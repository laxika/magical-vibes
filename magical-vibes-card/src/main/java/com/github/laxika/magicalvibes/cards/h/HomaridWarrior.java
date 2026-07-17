package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "92")
public class HomaridWarrior extends Card {

    public HomaridWarrior() {
        // {U}: This creature gains shroud until end of turn and doesn't untap during your next untap step. Tap it.
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF),
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)),
                "{U}: This creature gains shroud until end of turn and doesn't untap during your next untap step. Tap it."));
    }
}
