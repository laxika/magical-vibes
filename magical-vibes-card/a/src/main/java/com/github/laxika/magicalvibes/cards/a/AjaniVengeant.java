package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "154")
@CardRegistration(set = "DDH", collectorNumber = "1")
public class AjaniVengeant extends Card {

    public AjaniVengeant() {
        // +1: Target permanent doesn't untap during its controller's next untap step.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SkipNextUntapEffect(Subjects.target())),
                "+1: Target permanent doesn't untap during its controller's next untap step."));

        // −2: Ajani Vengeant deals 3 damage to any target and you gain 3 life. The fixed 3 life is
        // gained whenever the ability resolves, independent of damage dealt/prevented.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(DealDamageEffect.toAnyTarget(3), new GainLifeEffect(3)),
                "−2: Ajani Vengeant deals 3 damage to any target and you gain 3 life."));

        // −7: Destroy all lands target player controls.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(DestroyTargetPermanentEffect.allTargetPlayerControls(new PermanentIsLandPredicate())),
                "−7: Destroy all lands target player controls."));
    }
}
