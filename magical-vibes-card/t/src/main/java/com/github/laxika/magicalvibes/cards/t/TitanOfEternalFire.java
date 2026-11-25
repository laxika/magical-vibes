package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "144")
@CardRegistration(set = "C20", collectorNumber = "163")
public class TitanOfEternalFire extends Card {

    public TitanOfEternalFire() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{R}",
                        List.of(DealDamageEffect.toAnyTarget(1)),
                        "{R}, {T}: This creature deals 1 damage to any target."
                ),
                Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
        ));
    }
}
