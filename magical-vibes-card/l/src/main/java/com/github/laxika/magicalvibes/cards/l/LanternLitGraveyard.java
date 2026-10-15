package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "278")
public class LanternLitGraveyard extends Card {

    public LanternLitGraveyard() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {B} or {R}. This land doesn't untap during your next untap step.
        // "Add A or B" is modeled as two separate mana abilities; the SELF skip-untap rider
        // resolves inline with the mana in ActivatedAbilityExecutionService.doResolveManaAbility.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {B}. Lantern-Lit Graveyard doesn't untap during your next untap step."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}: Add {R}. Lantern-Lit Graveyard doesn't untap during your next untap step."
        ));
    }
}
