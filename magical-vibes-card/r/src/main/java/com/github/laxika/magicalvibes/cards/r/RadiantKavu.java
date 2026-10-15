package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "120")
public class RadiantKavu extends Card {

    public RadiantKavu() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}{W}",
                List.of(PreventDamageEffect.allCombatExcept(
                        new PermanentNotPredicate(new PermanentColorInPredicate(
                                Set.of(CardColor.BLUE, CardColor.BLACK))))),
                "{R}{G}{W}: Prevent all combat damage blue creatures and black creatures would deal this turn."
        ));
    }
}
