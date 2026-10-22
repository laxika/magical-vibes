package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "286")
public class WaterveilCavern extends Card {

    public WaterveilCavern() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {U} or {B}. This land doesn't untap during your next untap step.
        // The restriction is about the ability controller's untap step only, so it does not carry over to a new
        // controller's untap step (controllerStepOnly).
        // "Add A or B" is modeled as two separate mana abilities; the SELF skip-untap rider
        // resolves inline with the mana in ActivatedAbilityExecutionService.doResolveManaAbility.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)),
                "{T}: Add {U}. Waterveil Cavern doesn't untap during your next untap step."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)),
                "{T}: Add {B}. Waterveil Cavern doesn't untap during your next untap step."
        ));
    }
}
