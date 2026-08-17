package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "329")
@CardRegistration(set = "TPR", collectorNumber = "247")
@CardRegistration(set = "BRB", collectorNumber = "93")
public class VecTownships extends Card {

    public VecTownships() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {G} or {W}. This land doesn't untap during your next untap step.
        // "Add {G} or {W}" is modeled as two separate mana abilities (same idiom as Mogg Hollows).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SkipNextUntapEffect(TapUntapScope.SELF), new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}. Vec Townships doesn't untap during your next untap step."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SkipNextUntapEffect(TapUntapScope.SELF), new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}. Vec Townships doesn't untap during your next untap step."
        ));
    }
}
