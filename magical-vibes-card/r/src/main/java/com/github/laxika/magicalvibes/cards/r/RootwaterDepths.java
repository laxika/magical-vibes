package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "323")
@CardRegistration(set = "TPR", collectorNumber = "241")
public class RootwaterDepths extends Card {

    public RootwaterDepths() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {U}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {U}. This land doesn't untap during your next untap step."
        ));
        // {T}: Add {B}. This land doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {B}. This land doesn't untap during your next untap step."
        ));
    }
}
