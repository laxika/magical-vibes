package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "244")
public class SunWarriors extends Card {

    public SunWarriors() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK,
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, creaturesYouControl));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new CreateTokenEffect(
                        1, "Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of())),
                "{5}: Create a 1/1 white Ally creature token."
        ));
    }
}
