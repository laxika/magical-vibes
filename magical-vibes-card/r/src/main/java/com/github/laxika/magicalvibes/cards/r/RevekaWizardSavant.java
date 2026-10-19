package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "37")
@CardRegistration(set = "ME3", collectorNumber = "49")
public class RevekaWizardSavant extends Card {

    public RevekaWizardSavant() {
        // {T}: Reveka deals 2 damage to any target and doesn't untap during your next untap step.
        // The "doesn't untap" rider acts on the source itself and is bound to its controller's next untap step
        // (controllerStepOnly), so a control change does not carry the skip into the new controller's untap step.
        // The any-target choice comes from the damage effect's spec.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(
                        new DealDamageToAnyTargetEffect(2),
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)
                ),
                "{T}: Reveka, Wizard Savant deals 2 damage to any target and doesn't untap during your next untap step."
        ));
    }
}
