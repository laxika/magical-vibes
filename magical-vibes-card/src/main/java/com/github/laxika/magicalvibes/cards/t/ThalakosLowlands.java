package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "328")
public class ThalakosLowlands extends Card {

    public ThalakosLowlands() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {W}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {W}. This land doesn't untap during your next untap step."
        ));
        // {T}: Add {U}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {U}. This land doesn't untap during your next untap step."
        ));
    }
}
