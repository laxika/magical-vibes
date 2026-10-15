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

@CardRegistration(set = "CHK", collectorNumber = "284")
public class TranquilGarden extends Card {

    public TranquilGarden() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {G} or {W}. This land doesn't untap during your next untap step.
        // "Add A or B" is modeled as two separate mana abilities; the SELF skip-untap rider
        // resolves inline with the mana in ActivatedAbilityExecutionService.doResolveManaAbility.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {G}. Tranquil Garden doesn't untap during your next untap step."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {W}. Tranquil Garden doesn't untap during your next untap step."
        ));
    }
}
