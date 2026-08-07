package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "317")
public class CinderMarsh extends Card {

    public CinderMarsh() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {B}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {B}. This land doesn't untap during your next untap step."
        ));
        // {T}: Add {R}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {R}. This land doesn't untap during your next untap step."
        ));
    }
}
