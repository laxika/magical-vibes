package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1230")
@CardRegistration(set = "SLD", collectorNumber = "1508")
@CardRegistration(set = "SLD", collectorNumber = "1554")
@CardRegistration(set = "SLD", collectorNumber = "1910")
public class RinAndSeriInseparable extends Card {

    public RinAndSeriInseparable() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.DOG),
                        List.of(new CreateTokenEffect("Cat", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.CAT), Set.of(), Set.of()))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.CAT),
                        List.of(new CreateTokenEffect("Dog", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.DOG), Set.of(), Set.of()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{G}{W}",
                List.of(
                        new DealDamageToAnyTargetEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.DOG), CountScope.CONTROLLER)),
                        new GainLifeEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.CAT), CountScope.CONTROLLER))
                ),
                "{R}{G}{W}, {T}: Rin and Seri deals damage to any target equal to the number of Dogs you control. "
                        + "You gain life equal to the number of Cats you control."
        ));
    }
}
